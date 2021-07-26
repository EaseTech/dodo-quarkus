package com.example.dodoquarqus.repository;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.List;

@Getter
@Setter
@Document(indexName = "component_meta" )
public class ComponentMeta {

    @Id
    private String name;

    private String description;

    private String version;

    private boolean generated = false;

    private BackendMeta backend;

    private List<ClassMeta> controllers;

    private List<ClassMeta> services;

    private List<ClassMeta> repositories;
}
