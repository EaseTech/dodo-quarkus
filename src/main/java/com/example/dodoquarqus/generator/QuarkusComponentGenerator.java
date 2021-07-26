package com.example.dodoquarqus.generator;

import com.example.dodoquarqus.comment.ComponentsClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class QuarkusComponentGenerator extends ComponentGenerator{

    public QuarkusComponentGenerator(@Autowired RestTemplate restTemplate, @Autowired @Qualifier("quarkusDependencies") Map<String, String> dependencies) {
        super(restTemplate, dependencies, "/template/quarkus/");
    }

//    @Override
//    public void prepareComponent(com.example.dodoquarqus.comment.Component component) {
//        ComponentsClass
//        generate()
//
//    }
}
