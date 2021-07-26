package com.example.dodoquarqus.processor;

import com.example.dodoquarqus.comment.*;
import com.example.dodoquarqus.repository.ClassMeta;
import com.google.common.base.CaseFormat;
import freemarker.template.Template;

import java.io.StringWriter;
import java.util.*;

import static com.example.dodoquarqus.generator.QuarkusESEntityGenerator.isLiteralProp;

public class ProcessorUtil {


    public static final String COM_EXAMPLE = "com.example.";

    public static String generateGetMethodName(APIObject apiObject) {
        if(apiObject.getName() != null) {
            return apiObject.getName();
        }
        String baseName;
        if(apiObject.getRequest().getPageable()) {
            baseName = "find_all_by_";
        } else {
            baseName = "find_by_";
        }
        String[] pathVariables = apiObject.getRequest().getPath().split("/");
        Set<String> params = new HashSet<>();
        for(String var: pathVariables) {
            if(var.trim().startsWith("{")) {
                var = var.replace("{", "").replace("}", "");
                String[] typeAndValue = var.split(":");
                params.add(typeAndValue[0]);
            }
        }

        if(!params.isEmpty()) {
            baseName = baseName.concat(String.join("_And_", params));
        } else {
            return "findAll";
        }
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, baseName);
    }

    private static String generateMethodName(APIObject apiObject, String baseName) {

        if(apiObject.getRequest().getBody() == null) {
            throw new RuntimeException("Request Body cannot be null");
        }
        if(apiObject.getName() != null) {
            return apiObject.getName();
        }
        Body body = apiObject.getRequest().getBody();
        if(body.getRef() != null) {
            baseName = baseName.concat(body.getRef().toLowerCase());
        }else {
            baseName = baseName.concat(body.getType());
        }
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, baseName);
    }

    public static String generatePutMethodName(APIObject apiObject) {

        return generateMethodName(apiObject, "update_");
    }

    public static String generatePostMethodName(APIObject apiObject) {
        return generateMethodName(apiObject, "create_");

    }

    public static String getControllerClassName(APIObject apiObject) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, getClassName(apiObject.getRequest().getPath())).concat("Controller");
    }

    public static String getServiceClassName(APIObject apiObject) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, getClassName(apiObject.getRequest().getPath())).concat("Service");

    }

    public static String getRepositoryClassName(APIObject apiObject) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, getEntityType(apiObject)).concat("Repository");
    }

    public static String getRepoReturnType(APIObject apiObject) {
        String type = apiObject.getResponse().getType();
        String itemType;
        if(apiObject.getResponse().getConvert() != null && apiObject.getResponse().getConvert().getFrom() != null) {
            itemType = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, apiObject.getResponse().getConvert().getFrom());
            if(isCollectionProperty(type)) {
                if(apiObject.getAsync()){
                    type = "flux";
                }
                return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, type).concat("<").concat(itemType).concat(">");
            }else {
                if(apiObject.getAsync()) {
                    return "Mono<".concat(itemType).concat(">");
                }
                return itemType;
            }
        } else return getReturnType(apiObject);
    }

    public static List<ClassMeta> fillMeta(String methodName, String name) {
        List<ClassMeta> classMeta = new ArrayList<>();
        ClassMeta com = new ClassMeta();
        com.setName(name);
        List<String> methodsList = new ArrayList<>();
        methodsList.add(methodName);
        com.setMethods(methodsList);
        classMeta.add(com);
        return classMeta;
    }

    public static String getControllerMethod(APIObject apiObject, Map<String, Object> input, Template template) {
        input.put("path", getPath(apiObject));
        input.put("params", generateControllerParam(input, apiObject));

        input.put("val", "service.".concat(input.get("methodname").toString()).concat("(").concat(generateCallingParam(apiObject).concat(")")));
        input.put("jwtRoles", apiObject.getRoles());
        return processMethod( input, template);

    }

    public static String getIdType(Items items)  {
        String idType = getIdType(items.getProperties());
        if(idType == null) {
            throw new RuntimeException("No Id Type defined for Entity".concat(items.getType()));
        }
        return idType;
    }
    public static String getIdType(Set<Prop> properties){
        if(properties != null){
            for(Prop prop : properties) {
                if(isLiteralProp(prop)) {
                    if(prop.getName().equalsIgnoreCase("id")) {
                        return prop.getType();
                    }
                }
            }
        }
        return "";
    }

    public static String generateServiceParam(APIObject apiObject) {
        String paramString = "";
        Map<String, String> nameAndType = getPathParamNameAndType(apiObject);
        getBodyParamNameAndType(apiObject, nameAndType);
        for(String name: nameAndType.keySet()) {
            paramString = paramString.concat(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, nameAndType.get(name))).concat(" ").concat(name).concat(" , ");
        }
        if(apiObject.getRequest().getPageable()) {
            paramString = paramString.concat(" Pageable pageable").concat(" , ");
        }
        if(!paramString.isEmpty()) {
            return paramString.substring(0, paramString.lastIndexOf(",")-1);
        }
        return "";

    }

    public static String getConverterName(APIObject apiObject) {
        if(apiObject.getResponse().getConvert() == null || apiObject.getResponse().getConvert().getFrom() == null) {
            return null;
        }
        String from = apiObject.getResponse().getConvert().getFrom();
        String to = getBaseReturnType(apiObject);
        String converterName = from.concat("_to_").concat(to).concat("_converter");
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, converterName);
    }

    public static ComponentsClass getConverterClassDetails(IntermediateData id, String converterName, APIObject apiObject) {
        for(ComponentsClass cc: id.getConverterClasses()) {
            if(cc.getName().equalsIgnoreCase(converterName)) {
                return cc;
            }
        }
        ComponentsClass converterDetails = new ComponentsClass();
        converterDetails.setPackageName(COM_EXAMPLE.concat(id.getComponentName().toLowerCase()).concat(".converter;"));
        converterDetails.setClassType(ClassTypes.converter);
        converterDetails.setName(converterName);
        converterDetails.setComponentName(apiObject.getComponent().getRef());
        converterDetails.setComponentType(id.getComponentType());
        converterDetails.setConverterPath(COM_EXAMPLE.concat(id.getComponentName().toLowerCase()).concat(".converter.*;"));
        converterDetails.setEntityPath(COM_EXAMPLE.concat(id.getComponentName().toLowerCase()).concat(".entity.*;"));
        String fromClass = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, apiObject.getResponse().getConvert().getFrom());
        String toClass = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, getBaseReturnType(apiObject));
        converterDetails.setFromClass(fromClass);
        converterDetails.setToClass(toClass);
        if(apiObject.getAsync()){
            converterDetails.setAsync(true);
        }else {
            converterDetails.setAsync(false);
        }
        id.getConverterClasses().add(converterDetails);
        return converterDetails;
    }

    public static String getBaseReturnType(APIObject apiObject) {
        String type = apiObject.getResponse().getType();
        if(isCollectionProperty(type)) {
            Items items = apiObject.getResponse().getItems();
            String itemType = "";
            if(items.getRef() != null) {
                return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, items.getRef());
            } else {
                //If it is not a ref then it should be in line response object.
                return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, items.getType());
            }
        } else {
            return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, type);
        }
    }

    public static Boolean converterSpecified(APIObject apiObject) {
        if(apiObject.getResponse().getConvert() != null && apiObject.getResponse().getConvert().getFrom() != null) {
            return true;
        }else return false;
    }

//    public static String getComponentPath(String baseDir, String compName) {
//        return baseDir.concat(compName);
//    }

    public static String getMethodList(Set<String> methods) {
        if(methods == null) {
            return "";
        }
        StringBuffer b = new StringBuffer();
        for(String method : methods) {
            b.append(method);
            b.append(System.lineSeparator());
        }
        return b.toString();
    }

    public static String getEntityType(APIObject apiObject) {
        if(apiObject.getResponse().getConvert() != null && apiObject.getResponse().getConvert().getFrom() != null) {
            return apiObject.getResponse().getConvert().getFrom();
        }else if(apiObject.getResponse().getRef() != null) {
            return apiObject.getResponse().getRef();
        } else {
            String type = apiObject.getResponse().getType();
            if (isCollectionProperty(type)) {
                Items items = apiObject.getResponse().getItems();
                if(items.getRef() != null) {
                    return items.getRef();
                }else {
                    return items.getType();
                }
            }else {
                return type;
            }

        }

    }

    public static ComponentsClass getControllerClassDetails(IntermediateData id, String controllerName) {
        for(ComponentsClass cc: id.getControllerClasses()) {
            if(cc.getName().equalsIgnoreCase(controllerName)) {
                return cc;

            }
        }
        ComponentsClass controllerClass = new ComponentsClass();
        controllerClass.setName(controllerName);
        controllerClass.setClassType(ClassTypes.controller);
        fillData(controllerClass, id);
        id.getControllerClasses().add(controllerClass);
        return controllerClass;
    }

    public static void fillData(ComponentsClass cc, IntermediateData id) {
        cc.setComponentName(id.getComponentName());
        cc.setPackageName(COM_EXAMPLE.concat(id.getComponentName().toLowerCase()).concat(".controller;"));
        cc.setServicePath(COM_EXAMPLE.concat(id.getComponentName().toLowerCase()).concat(".service.*;"));
        cc.setEntityPath(COM_EXAMPLE.concat(id.getComponentName().toLowerCase()).concat(".entity.*;"));
        cc.setRepositoryPath(COM_EXAMPLE.concat(id.getComponentName().toLowerCase()).concat(".repository.*;"));


        cc.setOverride(id.isOverride());

    }

    public static String processMethod(Map<String, Object> input, Template template) {
        try {
//            Template template = cfg.getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.process(input, writer);
            return writer.toString();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String generateCallingParam(APIObject apiObject) {
        String paramString = "";
        Map<String, String> nameAndType = getPathParamNameAndType(apiObject);
        getBodyParamNameAndType(apiObject, nameAndType);
        for(String name: nameAndType.keySet()) {
            paramString = paramString.concat(name).concat(" , ");
        }
        if(apiObject.getRequest().getPageable()) {
            paramString = paramString.concat(" pageable ,");
        }
        if(paramString.isEmpty()) {
            return paramString;
        }
        return paramString.substring(0, paramString.lastIndexOf(",")-1);
    }

    public static void getBodyParamNameAndType(APIObject apiObject, Map<String, String> valueAndTypeMap) {
        Body body = apiObject.getRequest().getBody();
        if(body != null) {
            if(body.getRef() != null) {
                String type = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, body.getRef());
                String name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, body.getRef());
                valueAndTypeMap.put(name, type);
            }
        }

    }

    public static String getPath(APIObject apiObject) {
        String path = apiObject.getRequest().getPath()
                .replaceAll(":string", "")
                .replaceAll(":integer", "")
                .replaceAll(":long", "")
                .replaceAll(":boolean", "")
                .replaceAll(":float", "")
                .replaceAll("double", "");

        return path;

    }

    public static String generateControllerParam(Map<String, Object> input, APIObject apiObject) {
        if(apiObject.getRequest().getType().equalsIgnoreCase("post")) {
            return generatePostRequestParam(apiObject);
        } else if(apiObject.getRequest().getType().equalsIgnoreCase("get")) {
            return generateGetRequestParam(input, apiObject);
        }else if(apiObject.getRequest().getType().equalsIgnoreCase("put")) {
            String paramString = generatePostRequestParam(apiObject);
            paramString = paramString.concat(" , ").concat(generateGetRequestParam(input, apiObject));
            return paramString;
        }
        return "";

    }

    public static Map<String, String> getPathParamNameAndType(APIObject apiObject) {
        Map<String, String> valueAndTypeMap = new HashMap<>();
        String[] pathVariables = apiObject.getRequest().getPath().split("/");
        for(String var: pathVariables) {
            if(var.startsWith("{")) {
                String type = "";
                String name = "";
                //It is a Path Variable. Fetch the name and type
                var = var.replaceAll("\\{", "").replaceAll("}", "");
                String[] valueAndType = var.split(":");
                if(Arrays.stream(valueAndType).count() < 2) {
                    //Type is not provided, default to String
                    type = "String";
                    name = valueAndType[0];
                }else {
                    name = valueAndType[0];
                    type = valueAndType[1];
                }
                valueAndTypeMap.put(name, type);
            }
        }

        return valueAndTypeMap;
    }

    public static String generateGetRequestParam(Map<String, Object> input, APIObject apiObject) {
        String paramString = "";
        Map<String, String> nameAndType = getPathParamNameAndType(apiObject);
        for(String name: nameAndType.keySet()) {
            if(input.get("componentType").equals("spring")) {
                paramString = paramString.concat("@PathVariable ").concat(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, nameAndType.get(name))).concat(" ").concat(name).concat(" , ");
            }else if(input.get("componentType").equals("quarkus")) {
                paramString = paramString.concat(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, nameAndType.get(name))).concat(" ").concat(name).concat(" , ");
            }

        }
        if(apiObject.getRequest().getPageable()) {
            paramString = paramString.concat(" Pageable pageable").concat(" , ");
        }
        if(paramString.isEmpty()) {
            return paramString;
        }
        return paramString.substring(0, paramString.lastIndexOf(",")-1);
    }


    public static String generatePostRequestParam(APIObject apiObject) {

        Body body = apiObject.getRequest().getBody();
        if(body != null) {
            return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, body.getRef()).concat(" ").concat(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, body.getRef()));
        } else return "";
    }

    public static String getReturnType(APIObject apiObject) {
        String type = apiObject.getResponse().getType();
        if (isCollectionProperty(type)) {
            if(apiObject.getAsync()) {
                type = "flux";
            }
            else if(apiObject.getRequest().getPageable()) {
                type = "Page";
            }
            Items items = apiObject.getResponse().getItems();
            String itemType = "";
            if(items.getRef() != null) {
                itemType = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, items.getRef());
            } else {
                //If it is not a ref then it should be in line response object.
                itemType = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, items.getType());
            }

            return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, type).concat("<").concat(itemType).concat(">");

        } else {
            if(apiObject.getAsync()) {
                return "Mono<".concat(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, type)).concat(">");
            }
            return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, type);
        }
    }

    public static boolean isCollectionProperty(String type) {
        if(type.equalsIgnoreCase("list") || type.equalsIgnoreCase("set")) {
            return true;
        }
        return false;
    }

    public static String getClassName(String path) {
        return path.split("/")[1];
    }
}
