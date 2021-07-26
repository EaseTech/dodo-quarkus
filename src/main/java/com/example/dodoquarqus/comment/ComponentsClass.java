package com.example.dodoquarqus.comment;

import com.google.common.base.CaseFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Setter

public class ComponentsClass {

    private ClassTypes classType;
    private String name;
    private String serviceName;
    private String componentName;
    private String servicePath;
    private String entityPath;
    private String repositoryPath;
    private String packageName;
    private String entityType;
    private String idType;
    private boolean override;
    private String indexName;
    private String fromClass;
    private String toClass;
    private String converterPath;
    private String jwt;
    private String componentType;
    private boolean async;

    public void setIndexName(String indexName) {
        this.indexName = indexName;
        this.inputData.put("indexName", indexName);
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
        this.inputData.put("jwt", jwt);
    }

    public void setAsync(boolean async) {
        this.async = async;
        this.inputData.put("async", async);
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
        this.inputData.put("componentType", componentType);
    }

    public void setConverterPath(String converterPath) {
        this.converterPath = converterPath;
        this.inputData.put("converterPath", converterPath);
    }

    public void setFromClass(String fromClass) {
        this.fromClass = fromClass;
        this.inputData.put("fromClass", fromClass);
    }

    public void setToClass(String toClass) {
        this.toClass = toClass;
        this.inputData.put("toClass", toClass);
    }

    private Set<String> methods = new HashSet<>();

    public void setName(String name) {
        this.name = name;
        this.inputData.put("className", name);
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
        this.inputData.put("serviceName", serviceName);
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
        this.inputData.put("componentName", CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, componentName));
    }

    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
        this.inputData.put("servicePath", servicePath);
    }

    public void setEntityPath(String entityPath) {
        this.entityPath = entityPath;
        this.inputData.put("entityPath", entityPath);
    }

    public void setRepositoryPath(String repositoryPath) {
        this.repositoryPath = repositoryPath;
        this.inputData.put("repositoryPath", repositoryPath);
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
        this.inputData.put("package", packageName);
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
        this.inputData.put("entityType", entityType);
    }

    public void setIdType(String idType) {
        this.idType = idType;
        this.inputData.put("idType", idType);
    }

    public void setOverride(boolean override) {
        this.override = override;
        this.inputData.put("override", override);
    }

    public void setMethods(Set<String> methods) {
        this.methods = methods;
        this.inputData.put("methods", methods);
    }

    private Map<String, Object> inputData = new HashMap<>();
}
