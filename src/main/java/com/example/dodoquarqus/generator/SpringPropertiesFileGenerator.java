package com.example.dodoquarqus.generator;

import org.springframework.stereotype.Component;

@Component
public class SpringPropertiesFileGenerator extends PropertiesFileGenerator{

    public SpringPropertiesFileGenerator() {
        super("/template/spring/");
    }
}
