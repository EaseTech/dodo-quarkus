package com.example.dodoquarqus.generator;

import com.example.dodoquarqus.comment.ComponentsClass;
import com.example.dodoquarqus.comment.IntermediateData;

import java.util.Collections;
import java.util.List;


public class ClassGenerator extends Generator{

    public ClassGenerator(String basePackagePath) {
        super(basePackagePath);
    }

    public void generate(List<IntermediateData> idList) {
        for(IntermediateData id: idList) {
            handle(id.getControllerClasses());
            handle(id.getServiceClasses());
            handle(id.getRepositoryClasses());
            handle(id.getConverterClasses());
//            handle(Collections.singletonList(getJsonUtils()));

        }

    }

    private void handle(List<ComponentsClass> controllers) {
        for(ComponentsClass cc: controllers) {
            generate(cc);
        }
    }

}
