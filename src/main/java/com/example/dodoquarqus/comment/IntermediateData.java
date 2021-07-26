package com.example.dodoquarqus.comment;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class IntermediateData {

    private String componentName;

    private String enableJWT = "false";

    private boolean override = true;

    private String componentType;

    private String idType;

    private List<ComponentsClass> controllerClasses = new ArrayList<>();

    private List<ComponentsClass> serviceClasses = new ArrayList<>();

    private List<ComponentsClass> repositoryClasses = new ArrayList<>();

    private List<ComponentsClass> converterClasses = new ArrayList<>();
}
