package com.example.dodoquarqus.processor;

import com.example.dodoquarqus.comment.*;
import com.example.dodoquarqus.repository.DodoRepository;
import com.google.common.base.CaseFormat;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static com.example.dodoquarqus.processor.ProcessorUtil.getEntityType;

@org.springframework.stereotype.Component
public class SpringPreProcessor extends PreProcessor{


    public SpringPreProcessor(@Autowired DodoRepository dodoRepository) {
        super("/template/spring/", dodoRepository);
    }

    public List<IntermediateData> preprocess(List<APIObject> apiObjects, List<Component> components) {
        List<IntermediateData> idList = new ArrayList<>();
        for(APIObject apiObject: apiObjects) {
            //First get the intermediate data for the component to which this API belongs.
            IntermediateData intermediateData = getExistingData(idList, apiObject, components);
            fillConverterDetails(intermediateData, apiObject);
            //Next fill in the information about the Intermediate Data
            fillDetails(intermediateData, apiObject);

        }
        return idList;
    }

    @Override
    public void processRepository(IntermediateData id, APIObject apiObject, String methodName, String repositoryName) throws Exception {
        Map<String, Object> input = getInputDataForRepositoryMethod(apiObject, methodName);
        input.put(JWT, id.getEnableJWT());
        final var customRepositoryName = repositoryName.concat("Custom");
        final var customRepositoryImplName = repositoryName.concat("Impl");

        if(isCustomMethod(input.get("methodname").toString())) {
            fillRepositoryDetails(id, apiObject, customRepositoryName, input, "ESRepositoryMethod.ftl", ClassTypes.es_custom_repository);

            fillRepositoryDetails(id, apiObject, customRepositoryImplName, input, "ESRepositoryMethodCustomImpl.ftl", ClassTypes.es_custom_impl_repository);

        }else {
            if(! isSpringCRUDMethod(methodName)) {
                fillRepositoryDetails(id, apiObject, repositoryName, input, "ESRepositoryMethod.ftl", ClassTypes.es_repository);
            }


        }

    }

    private ComponentsClass fillRepositoryDetails(IntermediateData id, APIObject apiObject, String repositoryName, Map<String, Object> input, String ftl, ClassTypes ct) throws Exception{
        String repositoryMethod = getRepositoryMethod(apiObject, input, ftl);


        ComponentsClass repositoryDetails = getRepositoryClassDetails(id, apiObject, repositoryName, ct);
        repositoryDetails.setEntityType(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, getEntityType(apiObject)));

        Set<String> repositoryMethods = repositoryDetails.getMethods();
        if(repositoryMethods == null) {
            repositoryMethods = new HashSet<>();
        }
        repositoryMethods.add(repositoryMethod);
        repositoryDetails.setMethods(repositoryMethods);
        return repositoryDetails;
    }

    public boolean isCustomMethod(String methodName) {
        if(methodName == null){
            return false;
        }
        if(methodName.startsWith("find")
                || methodName.startsWith("findAll")
                || methodName.equalsIgnoreCase("save")
                || methodName.equalsIgnoreCase("delete")
                || methodName.equalsIgnoreCase("findById")
                || methodName.equalsIgnoreCase("count")
                || methodName.equalsIgnoreCase("exists")){
            return false;
        }
        return true;
    }

    public boolean isSpringCRUDMethod(String methodName) {
        if(methodName.equalsIgnoreCase("save")
                || methodName.equalsIgnoreCase("delete")
                || methodName.equalsIgnoreCase("findById")
                || methodName.equalsIgnoreCase("count")
                || methodName.equalsIgnoreCase("exists")){
            return true;
        }
        return false;
    }
}
