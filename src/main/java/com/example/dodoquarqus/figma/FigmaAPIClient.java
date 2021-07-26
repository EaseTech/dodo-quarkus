package com.example.dodoquarqus.figma;

import com.example.dodoquarqus.comment.*;
import com.example.dodoquarqus.generator.*;
import com.example.dodoquarqus.processor.QuarkusPreProcessor;
import com.example.dodoquarqus.processor.SpringPreProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
public class FigmaAPIClient {

    @Value("${figma.url}")
    private String FIGMA_URL;

    @Value("${figma.fileId}")
    private String FILE_ID;

    RestTemplate restTemplate;

    QuarkusPreProcessor quarkusPreProcessor;

    SpringPreProcessor springPreProcessor;

    SpringComponentGenerator springComponentGenerator;

    QuarkusComponentGenerator quarkusComponentGenerator;

    QuarkusClassGenerator quarkusClassGenerator;

    SpringClassGenerator springClassGenerator;

    EntityClassGenerator entityClassGenerator;

    QuarkusPOMGenerator pomGenerator;

    SpringPOMGenerator springPOMGenerator;

    SpringPropertiesFileGenerator springPropertiesFileGenerator;

    QuarkusPropertiesFileGenerator quarkusPropertiesFileGenerator;

    public FigmaAPIClient(@Autowired RestTemplate restTemplate,
                          @Autowired QuarkusPreProcessor quarkusPreProcessor,
                          @Autowired SpringComponentGenerator springComponentGenerator,
                          @Autowired QuarkusComponentGenerator quarkusComponentGenerator,
                          @Autowired QuarkusClassGenerator quarkusClassGenerator,
                          @Autowired SpringClassGenerator springClassGenerator,
                          @Autowired EntityClassGenerator entityClassGenerator,
                          @Autowired QuarkusPOMGenerator pomGenerator,
                          @Autowired SpringPropertiesFileGenerator springPropertiesFileGenerator,
                          @Autowired QuarkusPropertiesFileGenerator quarkusPropertiesFileGenerator,
                          @Autowired SpringPreProcessor springPreProcessor,
                          @Autowired SpringPOMGenerator springPOMGenerator) {
        this.restTemplate = restTemplate;
        this.quarkusPreProcessor = quarkusPreProcessor;
        this.springComponentGenerator = springComponentGenerator;
        this.quarkusComponentGenerator = quarkusComponentGenerator;
        this.quarkusClassGenerator = quarkusClassGenerator;
        this.springClassGenerator = springClassGenerator;
        this.entityClassGenerator = entityClassGenerator;
        this.pomGenerator = pomGenerator;
        this.springPropertiesFileGenerator = springPropertiesFileGenerator;
        this.quarkusPropertiesFileGenerator = quarkusPropertiesFileGenerator;
        this.springPreProcessor = springPreProcessor;
        this.springPOMGenerator = springPOMGenerator;

    }

    public void call(String fileId) throws Exception {
        HttpEntity<String> requestEntity = new HttpEntity<>(null, getHeaders());
        ResponseEntity<Comments> comments = restTemplate.exchange(new URI(FIGMA_URL.replace("FILE_ID", fileId)), HttpMethod.GET, requestEntity, Comments.class);
        List<Component> quarkusComponents = new ArrayList<>();
        List<Component> springComponents = new ArrayList<>();
        List<String> quarkusComponentNames = new ArrayList<>();
        List<String> springComponentNames = new ArrayList<>();
        List<APIObject> quarkusApiObjects = new ArrayList<>();
        List<APIObject> springApiObjects = new ArrayList<>();
        List<APIObject> allAPIObjects = new ArrayList<>();

        for(FigmaComment comment: comments.getBody().getComments()) {
            try {
                DodoComment c = new ObjectMapper(new YAMLFactory()).readValue(comment.getMessage(), DodoComment.class);
                Component component = c.getComponent();
                if(component != null) {
                    if(component.getType().equalsIgnoreCase("quarkus")){
                        quarkusComponents.add(component);
                        quarkusComponentNames.add(component.getName());
                    }else {
                        springComponents.add(component);
                        springComponentNames.add(component.getName());
                    }
                } else {
                    APIObject api = c.getApi();
                    allAPIObjects.add(api);

                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        for(APIObject apiObject: allAPIObjects){
            if(quarkusComponentNames.contains(apiObject.getComponent().getRef())){
                quarkusApiObjects.add(apiObject);
            }else if(springComponentNames.contains(apiObject.getComponent().getRef())){
                springApiObjects.add(apiObject);
            }
        }

        List<IntermediateData> quarkusList = quarkusPreProcessor.preprocess(quarkusApiObjects, quarkusComponents);
        List<IntermediateData> springList = springPreProcessor.preprocess(springApiObjects, springComponents);
        for(Component component: quarkusComponents) {
            quarkusComponentGenerator.generateComponent(component);
            entityClassGenerator.generateEntities(component);
            pomGenerator.handlePom(component);
            quarkusPropertiesFileGenerator.generate(springComponents);
        }
        for(Component component: springComponents) {
            springComponentGenerator.generateComponent(component);
            entityClassGenerator.generateEntities(component);
            springPOMGenerator.generate(component);
//            pomGenerator.handlePom(component);
        }
        quarkusClassGenerator.generate(quarkusList);
        springClassGenerator.generate(springList);
        quarkusPropertiesFileGenerator.generate(quarkusComponents);
        springPropertiesFileGenerator.generate(springComponents);





    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-FIGMA-TOKEN", "191334-46f60878-0bf7-4749-aa0e-a3f6f6aa37df");
        return headers;

    }

    @Scheduled(fixedDelay = 6000, initialDelay = 500)
    public void schedule() throws Exception {
        String fileId = FILE_ID;
        call(fileId);
    }
}
