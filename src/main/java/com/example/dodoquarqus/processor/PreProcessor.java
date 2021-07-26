package com.example.dodoquarqus.processor;

import com.example.dodoquarqus.comment.*;
import com.example.dodoquarqus.repository.ClassMeta;
import com.example.dodoquarqus.repository.ComponentMeta;
import com.example.dodoquarqus.repository.DodoRepository;
import com.google.common.base.CaseFormat;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

import java.util.*;

import static com.example.dodoquarqus.processor.ProcessorUtil.*;
import static freemarker.template.Configuration.SQUARE_BRACKET_INTERPOLATION_SYNTAX;

public abstract class PreProcessor {

    public static final String JWT = "jwt";

    protected DodoRepository dodoRepository;

    public Configuration cfg = null;

    public PreProcessor(String basePackagePath, DodoRepository dodoRepository) {
        cfg = new Configuration(new Version(2, 3, 20));
        cfg.setClassForTemplateLoading(PreProcessor.class, basePackagePath);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setInterpolationSyntax(SQUARE_BRACKET_INTERPOLATION_SYNTAX );
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        this.dodoRepository = dodoRepository;
    }

    public IntermediateData getExistingData(List<IntermediateData> idList, APIObject apiObject, List<Component> components) {

        for(IntermediateData existingId: idList) {
            if(existingId.getComponentName().equalsIgnoreCase(apiObject.getComponent().getRef())) {
                return existingId;
            }
        }
        IntermediateData id = new IntermediateData();
        id.setComponentName(apiObject.getComponent().getRef());

        setOtherProperties(id, apiObject, components);
        idList.add(id);
        return id;
    }

    public void setOtherProperties(IntermediateData id, APIObject apiObject, List<Component> components) {
        for(Component c: components) {
            if(c.getName().equalsIgnoreCase(id.getComponentName())) {
                id.setOverride(c.isOverride());
                id.setEnableJWT(c.getEnableJWT());
                id.setComponentType(c.getType());
                id.setIdType(getIdType(apiObject, c));

            }
        }
    }

    public void setOtherProperties(IntermediateData id, List<Component> components) {
        for(Component c: components) {
            if(c.getName().equalsIgnoreCase(id.getComponentName())) {
                id.setOverride(c.isOverride());
                id.setEnableJWT(c.getEnableJWT());
                id.setComponentType(c.getType());
            }
        }
    }

    public String getIdType(APIObject apiObject, Component component) {
        String entityType = getEntityType(apiObject);
        for(Entity entity : component.getBackend().getEntities()){
            if(entity.getType().equalsIgnoreCase(entityType)){
                return ProcessorUtil.getIdType(entity.getProperties());
            }
        }
        throw new RuntimeException("Entity Id not found");
    }

    public void fillConverterDetails(IntermediateData id, APIObject apiObject) {
        final var converterName = getConverterName(apiObject);
        if(converterName != null) {
            getConverterClassDetails(id, converterName, apiObject);
        }
    }

    public void fillDetails(IntermediateData id, APIObject apiObject) {
        if(apiObject.getRequest().getType().equals("get")) {
            String methodName = generateGetMethodName(apiObject);
            getMethod(id, apiObject, methodName, Templates.get.getTemplate());

        }
        else if(apiObject.getRequest().getType().equals("put")) {
            String methodName = generatePutMethodName(apiObject);
            getMethod(id, apiObject, methodName, Templates.put.getTemplate());

        } else if(apiObject.getRequest().getType().equals("post")) {
            String methodName = generatePostMethodName(apiObject);
            getMethod(id, apiObject, methodName, Templates.post.getTemplate());
        }

//        } else if(apiObject.getRequest().getType().equals("delete")) {

//            getDeleteMethod(id, apiObject);
//        }

    }

    private void getMethod(IntermediateData id, APIObject apiObject,  String methodName, String templateName) {
        final var controllerName = getControllerClassName(apiObject);
        final var serviceName = getServiceClassName(apiObject);
        final var repositoryName = getRepositoryClassName(apiObject);
        if (!id.isOverride()) {
            if (methodAlreadyExists(apiObject, methodName))
                //Do nothing, since we dont want to override.
                return;
        }
        try {
            processController(id, apiObject, methodName, templateName, serviceName, controllerName);
            processService(id, apiObject, methodName, serviceName);
            processRepository(id, apiObject, methodName, repositoryName);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processController(IntermediateData id, APIObject apiObject,  String methodName, String templateName, String serviceName, String controllerName) throws Exception{
        Map<String, Object> input = getInputDataForControllerMethod(apiObject, methodName);
        input.put(JWT, id.getEnableJWT() == null ? "false": id.getEnableJWT());
        input.put("componentType", id.getComponentType());
        String controllerMethod = getControllerMethod(apiObject, input, cfg.getTemplate(templateName));
        ComponentsClass controllerDetails = getControllerClassDetails(id, controllerName);
        controllerDetails.setEntityType(getEntityType(apiObject));
        controllerDetails.setServiceName(serviceName);
        controllerDetails.setJwt(id.getEnableJWT());
        controllerDetails.setAsync(apiObject.getAsync());

        Set<String> controllerMethods = controllerDetails.getMethods();
        if(controllerMethods == null) {
            controllerMethods = new HashSet<>();
        }
        controllerMethods.add(controllerMethod);
        controllerDetails.setMethods(controllerMethods);
    }

    private void processService(IntermediateData id, APIObject apiObject,  String methodName, String serviceName) throws Exception{
        Map<String, Object> input = getInputDataForServiceMethod(apiObject, methodName);
        input.put(JWT, id.getEnableJWT());
        String serviceMethod = getServiceMethod(apiObject, input);
        ComponentsClass serviceDetails = getServiceClassDetails(id, serviceName, apiObject);
        Set<String> serviceMethods = serviceDetails.getMethods();
        if(serviceMethods == null) {
            serviceMethods = new HashSet<>();
        }
        serviceMethods.add(serviceMethod);
        serviceDetails.setMethods(serviceMethods);
    }

    public abstract void processRepository(IntermediateData id, APIObject apiObject,  String methodName, String repositoryName) throws Exception;

    public void processBasicRepository(IntermediateData id, APIObject apiObject, String methodName, String repositoryName) throws Exception{
        Map<String, Object> input = getInputDataForRepositoryMethod(apiObject, methodName);
        input.put(JWT, id.getEnableJWT());
        String ftl = "ESRepositoryMethod.ftl";
        String repositoryMethod = getRepositoryMethod(apiObject, input, ftl);
        ComponentsClass repositoryDetails = getRepositoryClassDetails(id, apiObject, repositoryName, ClassTypes.es_repository);
        Set<String> repositoryMethods = repositoryDetails.getMethods();
        if(repositoryMethods == null) {
            repositoryMethods = new HashSet<>();
        }
        repositoryMethods.add(repositoryMethod);
        repositoryDetails.setMethods(repositoryMethods);

    }

    ComponentsClass getRepositoryClassDetails(IntermediateData id, APIObject apiObject, String repositoryName, ClassTypes ct) {
        for(ComponentsClass cc: id.getRepositoryClasses()) {
            if(cc.getName().equalsIgnoreCase(repositoryName)) {
                return cc;
            }
        }
        ComponentsClass repositoryClass = new ComponentsClass();
        repositoryClass.setName(repositoryName);
        repositoryClass.setClassType(ct);
        repositoryClass.setIdType(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL,id.getIdType()));
        fillData(repositoryClass, id);
        String entityType = getEntityType(apiObject);
        repositoryClass.setIndexName("".concat("\"").concat(entityType.toLowerCase()).concat("\""));
        repositoryClass.setEntityType(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL,getEntityType(apiObject)));

        repositoryClass.setPackageName(COM_EXAMPLE.concat(id.getComponentName().toLowerCase()).concat(".repository;"));
        id.getRepositoryClasses().add(repositoryClass);
        if(apiObject.getAsync()){
            repositoryClass.setAsync(true);
        }else {
            repositoryClass.setAsync(false);
        }
        return repositoryClass;
    }

    protected String getRepositoryMethod(APIObject apiObject, Map<String, Object> input, String ftl) throws Exception{
//        String ftl = "ESRepositoryMethod.ftl";
        input.put("params", generateServiceParam(apiObject));
        return processMethod( input, cfg.getTemplate(ftl));
    }


    private String getServiceMethod(APIObject apiObject, Map<String, Object> input) throws Exception{
        input.put("params", generateServiceParam(apiObject));
        input.put("val", "repository.".concat(input.get("methodname").toString()).concat("(").concat(generateCallingParam(apiObject).concat(")")));
        if(converterSpecified(apiObject)) {
            input.put("converterName", getConverterName(apiObject));
            return processMethod(input, cfg.getTemplate("methodWithConverter.ftl"));
        }else {
            if(input.get("methodname").toString().equalsIgnoreCase("findbyid")) {
                return processMethod(input, cfg.getTemplate("findById.ftl"));
            }
            return processMethod(input, cfg.getTemplate("method.ftl"));
        }

    }

    private ComponentsClass getServiceClassDetails(IntermediateData id, String serviceName, APIObject apiObject) {
        for(ComponentsClass cc: id.getServiceClasses()) {
            if(cc.getName().equalsIgnoreCase(serviceName)) {
                return cc;
            }
        }
        ComponentsClass serviceClass = new ComponentsClass();
        serviceClass.setName(serviceName);
        serviceClass.setConverterPath(COM_EXAMPLE.concat(id.getComponentName().toLowerCase()).concat(".converter.*;"));
        serviceClass.setClassType(ClassTypes.service);
        fillData(serviceClass, id);
        serviceClass.setPackageName(COM_EXAMPLE.concat(id.getComponentName().toLowerCase()).concat(".service;"));
        serviceClass.setEntityType(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL,getEntityType(apiObject)));
        id.getServiceClasses().add(serviceClass);
        if(apiObject.getAsync()){
            serviceClass.setAsync(true);
        }else {
            serviceClass.setAsync(false);
        }
        return serviceClass;
    }

    private Map<String,Object> getInputDataForServiceMethod(APIObject apiObject, String methodName) {
        Map<String,Object> input = getInputDataForMethod(apiObject, methodName);
        return input;
    }

    Map<String,Object> getInputDataForRepositoryMethod(APIObject apiObject, String methodName) {
        Map<String,Object> input = getInputDataForMethod(apiObject, methodName);
        input.put("return", getRepoReturnType(apiObject));

//        input.put("idType", getI)
        return input;
    }

    private Map<String,Object> getInputDataForControllerMethod(APIObject apiObject, String methodName) {
        Map<String,Object> input = getInputDataForMethod(apiObject, methodName);
        input.put("body", "");
        input.put("produces", apiObject.getRequest().getProduces());
        input.put("consumes", apiObject.getRequest().getConsumes());

        return input;
    }

    private Map<String,Object> getInputDataForMethod(APIObject apiObject, String methodName) {
        Map<String,Object> input = new HashMap<>();
        input.put("methodname", methodName);

        input.put("return", getReturnType(apiObject));
        if(apiObject.getAsync()){
            input.put("async", true);
        }else {
            input.put("async", false);
        }
        return input;

    }


    protected boolean methodAlreadyExists(APIObject apiObject, String methodName) {
        String componentName = apiObject.getComponent().getRef();
        String controllerName = getControllerClassName(apiObject);
        Optional<ComponentMeta> meta = dodoRepository.findById(componentName);
        if(meta.isPresent()) {
            ComponentMeta compMeta = meta.get();
            List<ClassMeta> controllerMeta = compMeta.getControllers();
            if(! controllerMeta.isEmpty()) {
                for(ClassMeta cm: controllerMeta) {
                    if(cm.getName().equals(controllerName)) {
                        if(cm.getMethods().contains(methodName)) {
                            //Method exists
                            return true;
                        }else {
                            //Controller exists, but method within controller doesnt exist
                            cm.getMethods().add(methodName);
                            dodoRepository.save(compMeta);
                            return false;
                        }
                    }
                }
                //We couldnt find the Controller in Meta.
                //Add the controller to component Meta
                compMeta.getControllers().addAll(fillMeta(methodName, controllerName));
                dodoRepository.save(compMeta);
                return false;
            }
        }else {
            //The component within which this Controller needs to be created hasn't been created yet.
            ComponentMeta cm = new ComponentMeta();
            cm.setName(componentName);
            cm.setControllers(fillMeta(methodName, controllerName));

            dodoRepository.save(cm);
            return false;

        }
        return false;
    }

}
