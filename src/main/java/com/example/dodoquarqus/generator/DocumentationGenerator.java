package com.example.dodoquarqus.generator;

import com.example.dodoquarqus.comment.Component;

@org.springframework.stereotype.Component
public class DocumentationGenerator extends Generator{

    public DocumentationGenerator() {
        super("basePackagePath");
    }

    public void generate(Component component){
        getComponentPath(component);
    }
}
