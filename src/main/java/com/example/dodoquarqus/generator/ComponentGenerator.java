package com.example.dodoquarqus.generator;

import com.example.dodoquarqus.comment.ClassTypes;
import com.example.dodoquarqus.comment.Component;
import com.example.dodoquarqus.comment.ComponentsClass;
import com.google.common.base.CaseFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.dodoquarqus.processor.ProcessorUtil.COM_EXAMPLE;

@Getter
@Setter
public abstract class ComponentGenerator extends Generator {

    private RestTemplate restTemplate;

    private Map<String, String> dependencies;




    @Value("${quarkus.generator.url}")
    private String quarkusGeneratorUrl;

    @Value("${spring.generator.url}")
    private String springGeneratorUrl;


    public ComponentGenerator(RestTemplate restTemplate, Map<String, String> dependencies, String basePackagePath) {
        super(basePackagePath);
        this.restTemplate = restTemplate;
        this.dependencies = dependencies;
    }

    public String generateComponent(Component component) throws IOException {
        List<String> dependencies = fetchDependenciesList(component);
        downloadComponent(component, dependencies);
        prepareComponent(component);

//

        return getComponentPath(component);
    }

    public void prepareComponent(com.example.dodoquarqus.comment.Component component){
        ComponentsClass cc = new ComponentsClass();
        cc.setPackageName(COM_EXAMPLE.concat(component.getName().toLowerCase()).concat(".utility;"));
        cc.setClassType(ClassTypes.json_util);
        cc.setComponentName(component.getName());
        cc.setName("");
        generate(cc);
    }

    public String getComponentPath(Component component) {
        return DIR_NAME.concat(component.getName());
    }



    private String downloadComponent(Component component, List<String> dependencies) throws IOException {
        String url = null;
        if(component.getType().equalsIgnoreCase("quarkus")) {
            url = quarkusGeneratorUrl.replaceAll("COMPONENT_NAME", component.getName()).replaceAll("DEPENDENCIES", String.join("&", dependencies));
        } else if(component.getType().equalsIgnoreCase("spring")) {
            url = springGeneratorUrl.replaceAll("COMPONENT_NAME", component.getName()).replaceAll("DEPENDENCIES", String.join(",", dependencies));
        }
        if(url != null){
            byte[] downloadedProject = restTemplate.getForObject(url, byte[].class);

            ZipHelper.unzip(downloadedProject, DIR_NAME);
            return getComponentPath(component);
        }
        return "";

    }

    private List<String> fetchDependenciesList(Component component) {
        List<String> d = new ArrayList<>();
        //We need Web Dependency
        if(component.getType() == null) {
            return new ArrayList<>();
        }
        if(component.getType().equalsIgnoreCase("quarkus")){
            d.add(dependencies.get("kubernetes"));
            d.add(dependencies.get("jib"));
        }
        if(dependencies == null) return new ArrayList<>();
        if(component.isAsync()){
            d.add(dependencies.get("async"));
        }else {
            d.add(dependencies.get("web"));
        }



        if(component.getBackend() != null && component.getBackend().getType() != null) {
            if(! dependencies.containsKey(component.getBackend().getType())) {
                throw new RuntimeException("Backend Type not found");
            }
            d.add(dependencies.get(component.getBackend().getType()));

        }
        if(component.getEnableJWT().equalsIgnoreCase("true")) {
            d.add(dependencies.get("jwt"));
        }

        return d;
    }




}
