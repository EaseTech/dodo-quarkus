package com.example.dodoquarqus.generator;

import com.example.dodoquarqus.comment.*;
import com.example.dodoquarqus.repository.BackendMeta;
import com.example.dodoquarqus.repository.ComponentMeta;
import com.example.dodoquarqus.repository.DodoRepository;
import com.google.common.base.CaseFormat;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import static com.example.dodoquarqus.processor.ProcessorUtil.getIdType;

public class EntityGenerator extends Generator{

    private DodoRepository dodoRepository;

    public EntityGenerator(String basePackagePath, DodoRepository dodoRepository) {
        super(basePackagePath);
        this.dodoRepository = dodoRepository;
    }

    void generateEntity(Component component) {


        generateBaseConverter(component);
        Set<String> methods = new HashSet<>();
        for(Entity entity: component.getBackend().getEntities()) {
            if(shouldGenerate(entity, component)) {
                ComponentsClass cc = fetchInputData(component, entity);
                cc.setClassType(ClassTypes.entity);
                cc.setName(entity.getType());
                generate(cc);
                generateExceptionClass(component, entity);
                methods.add(getExceptionMethod(entity));
            }
        }
        if(component.getFrontend() != null) {
            for(Entity entity: component.getFrontend().getEntities()) {
                if(shouldGenerate(entity, component)) {
                    ComponentsClass cc = fetchInputData(component, entity);
                    cc.setClassType(ClassTypes.pojo_entity);
                    cc.setName(entity.getType());
                    generate(cc);
                }
            }
        }

        ComponentsClass cc = new ComponentsClass();
        cc.setComponentName(component.getName());
        cc.setName("");
        cc.setPackageName("com.example.".concat(component.getName().toLowerCase()).concat(".exception;"));
        cc.setMethods(methods);
        cc.setClassType(ClassTypes.controller_advisor);
//        TODO: Revisit
//        generate(cc);
    }

    protected void generateBaseConverter(Component component) {
        ComponentsClass cc = new ComponentsClass();
        cc.setClassType(ClassTypes.base_converter);
        cc.setName("BaseConverter");
        cc.setPackageName("com.example.".concat(component.getName().toLowerCase()).concat(".converter;"));
        cc.setComponentName(component.getName());
        cc.setAsync(component.isAsync());
        generate(cc);
    }

    private String getExceptionMethod(Entity entity) {
        try {

            StringWriter sw = new StringWriter();
            Map<String, Object> input = new HashMap<>();
            input.put("entityType", CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, entity.getType()));
            Template template = cfg.getTemplate(ClassTypes.exception_method.getFtl());
            template.process(input, sw);
            return sw.toString();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return "";

    }
    private void generateExceptionClass(Component component, Entity entity) {
        ComponentsClass cc = new ComponentsClass();
        cc.setComponentName(component.getName());
        cc.setClassType(ClassTypes.exception_class);
        String entityType = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL,entity.getType());
        cc.setName(entityType);
        cc.setEntityType(entityType);
        cc.setPackageName("com.example.".concat(component.getName().toLowerCase()).concat(".exception;"));
        generate(cc);
    }

    private boolean shouldGenerate(Entity entity, Component component) {
        if(component.isOverride()) {
            return true;
        }
        Optional<ComponentMeta> metaOptional = dodoRepository.findById(component.getName());
        if(metaOptional.isPresent()) {
            ComponentMeta componentMeta = metaOptional.get();
            if(componentMeta.getBackend() == null || componentMeta.getBackend().getEntities() == null) {
                BackendMeta bm = new BackendMeta();
                bm.setType(component.getBackend().getType());
                List<String> entities = new ArrayList<>();
                entities.add(entity.getType());
                bm.setEntities(entities);
                componentMeta.setBackend(bm);
                dodoRepository.save(componentMeta);
                return true;
            }
            List<String> entities = componentMeta.getBackend().getEntities();
            if(entities != null && !entities.isEmpty()) {
                if(entities.contains(entity.getType())) {
                    return false;
                }else {
                    componentMeta.getBackend().getEntities().add(entity.getType());
                    dodoRepository.save(componentMeta);
                }
            }
        }
        return true;
    }

    private ComponentsClass fetchInputData(Component component, Entity entity) {
        ComponentsClass cc = new ComponentsClass();
        cc.setComponentName(component.getName());
        cc.setEntityType(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL,entity.getType()));
        cc.setIdType(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, getIdType(entity.getProperties())));
        cc.getMethods().add(getMethods(component, entity.getProperties(), getComponentPath(component)));
        cc.setIndexName("".concat("\"").concat(entity.getType().toLowerCase()).concat("\""));
        cc.setPackageName("com.example.".concat(component.getName().toLowerCase()).concat(".entity;"));

        return cc;

    }

    private ComponentsClass fetchInputData(Component component, Items items) {
        ComponentsClass cc = new ComponentsClass();
        cc.setComponentName(component.getName());
        cc.setName(items.getName());
        cc.setEntityType(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL,getType(items)));
        cc.setIdType(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, getIdType(items.getProperties())));
        cc.getMethods().add(getMethods(component, items.getProperties(), getComponentPath(component)));
        cc.setPackageName("com.example.".concat(component.getName().toLowerCase()).concat(".entity;"));

        return cc;

    }

    public String getComponentPath(Component component) {
        return DIR_NAME.concat(component.getName());
    }

    private Map<String, Object> input(Component component) {
        Map<String, Object> input = new HashMap<>();
        String componentName = component.getName();
        input.put("package", "com.example.".concat(componentName.toLowerCase()).concat(".entity;"));

        return input;

    }


    private String getType(Items items) {
        if(isLiteralProp(items.getType())) {
            return items.getType();
        }else return items.getName();
    }
//
//    private Map<String, Object> fetchInputDataForObjectType(Component component, Prop prop, String componentPath) {
//        Map<String, Object> input = input(component);
//        input.put("entityType", CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL,prop.getName()));
//        input.put("methods", getMethods(component, prop.getProperties(), componentPath));
//        return input;
//    }

    private String getMethods(Component component, Set<Prop> props, String componentPath) {
        StringBuffer sb = new StringBuffer();

//        sb.append(System.lineSeparator());
        try {
            if(props != null) {
                for(Prop prop: props) {
                    if(isLiteralProp(prop)) {
                        if(prop.getName().equalsIgnoreCase("id")) {
                            Template template = cfg.getTemplate("ESEntityIDProperty.ftl");
                            sb = getMethodString(sb, prop, template);
                        }
                        else if(prop.getType().equalsIgnoreCase("date")) {
                            Template template = cfg.getTemplate("ESEntityDateProperty.ftl");
                            sb = getMethodString(sb, prop, template);
                        }else {
                            Template template = cfg.getTemplate("ESEntityLiteralProperty.ftl");
                            sb = getMethodString(sb, prop, template);
                        }

                    } else if(isCollectionProperty(prop)) {

                        if(prop.getType().equalsIgnoreCase("list")) {
                            Template template = cfg.getTemplate("ESEntityListProperty.ftl");
                            sb = getMethodString(sb, prop, template);
                        } else if(prop.getType().equalsIgnoreCase("set")) {
                            Template template = cfg.getTemplate("ESEntitySetProperty.ftl");
                            sb = getMethodString(sb, prop, template);
                        }
                        handleCollectionEntity(component, prop.getItems());

                    } else if(isObjectProperty(prop)) {
//                        String componentName = component.getName();
                        Template template = cfg.getTemplate("ESEntityLiteralProperty.ftl");
                        sb = getMethodString(sb, prop, template);
                        ComponentsClass cc = new ComponentsClass();
                        cc.setName(prop.getName());
                        cc.setClassType(ClassTypes.es_nested_entity);
                        cc.setComponentName(component.getName());
                        cc.setPackageName("com.example.".concat(component.getName().toLowerCase()).concat(".entity;"));
                        cc.setEntityType(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL,prop.getName()));
                        cc.getMethods().add(getMethods(component, prop.getProperties(), componentPath));
                        generate(cc);
//                        generate(component, ClassTypes.es_nested_entity, fetchInputDataForObjectType(component, prop, componentPath), prop.getName());
                    }
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }


    private void handleCollectionEntity(Component component, Items items) {
        if(isLiteralProp(items.getType())) {
            return;
        }else {

            ComponentsClass cc = fetchInputData(component, items);
            cc.setComponentName(component.getName());
            cc.setClassType(ClassTypes.es_nested_entity);
            generate(cc);
            //It is a list of Custom properties



        }
    }

    public static boolean isLiteralProp(Prop prop) {
        return isLiteralProp(prop.getType());
    }

    public static boolean isLiteralProp(String type) {
        if(type.equalsIgnoreCase("integer") ||
                type.equalsIgnoreCase("boolean") ||
                type.equalsIgnoreCase("string") ||
                type.equalsIgnoreCase("long") ||
                type.equalsIgnoreCase("float") ||
                type.equalsIgnoreCase("double") ||
                type.equalsIgnoreCase("date") ||
                type.equalsIgnoreCase("char")) {
            return true;
        }
        return false;
    }

    public static boolean isCollectionProperty(String type) {
        if(type.equalsIgnoreCase("list") || type.equalsIgnoreCase("set")) {
            return true;
        }
        return false;
    }

    private static boolean isObjectProperty(Prop prop) {
        if(isCollectionProperty(prop) || isLiteralProp(prop)) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isCollectionProperty(Prop prop) {
        return isCollectionProperty(prop.getType());
    }

    private String getType(Prop prop) {
        String fieldType = prop.getType();
        if(isCollectionProperty(prop)) {
            Items items = prop.getItems();
            if(isLiteralProp(items.getType())) {
                fieldType = items.getType();
            } else {
                //TODO: handle infinite depth
                //Its an object property (assuming only 1 depth level)
                fieldType = items.getName();
            }
        }else if(isLiteralProp(prop)) {
            return prop.getType();
        }else if(isObjectProperty(prop)) {
            return prop.getName();
        }
        return fieldType;

    }

    private String fieldName(Prop prop) {
        String fieldName = prop.getName();
        if(isCollectionProperty(prop)) {
            fieldName = prop.getItems().getName();
        }
        return fieldName;
    }
    private StringBuffer getMethodString(StringBuffer sb, Prop prop, Template template) throws TemplateException, IOException {
        StringWriter stringWriter = new StringWriter();
        Map<String, String> input = new HashMap<>();
        input.put("fieldName", fieldName(prop));

        input.put("fieldType", CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, getType(prop)));
        template.process(input, stringWriter);
        sb = sb.append(stringWriter.toString().concat( System.lineSeparator()));
        return sb;
    }
}
