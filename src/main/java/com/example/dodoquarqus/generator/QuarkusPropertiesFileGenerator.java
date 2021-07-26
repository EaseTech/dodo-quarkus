package com.example.dodoquarqus.generator;

import org.springframework.stereotype.Component;

@Component
public class QuarkusPropertiesFileGenerator extends PropertiesFileGenerator{

    public QuarkusPropertiesFileGenerator() {
        super("/template/quarkus/");
    }
}
