package com.example.dodoquarqus.generator;

import org.springframework.stereotype.Component;

@Component
public class SpringClassGenerator extends ClassGenerator{

    public SpringClassGenerator() {
        super("/template/spring/");
    }
}
