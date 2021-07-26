package com.example.dodoquarqus.generator;

import org.springframework.stereotype.Component;

@Component
public class QuarkusClassGenerator extends ClassGenerator{

    public QuarkusClassGenerator() {
        super("/template/quarkus/");
    }
}
