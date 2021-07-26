package com.example.dodoquarqus.processor;

import com.example.dodoquarqus.comment.*;
import com.example.dodoquarqus.repository.ClassMeta;
import com.example.dodoquarqus.repository.ComponentMeta;
import com.example.dodoquarqus.repository.DodoRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Component
public class QuarkusPreProcessor extends PreProcessor{

    public static final String COM_EXAMPLE = "com.example.";

    public QuarkusPreProcessor(@Autowired DodoRepository dodoRepository) {
        super("/template/quarkus/", dodoRepository);
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
        for(Component component: components) {
            IntermediateData id = getExistingData(idList, component);

            if(classAlreadyExists(component, "BaseRepository")) {
                break;
            } else {
                processBaseRepository(id, component);
            }
        }

        return idList;
    }



    private Boolean classAlreadyExists(Component component, String className) {
        Optional<ComponentMeta> optionalMeta = dodoRepository.findById(component.getName());
        if(optionalMeta.isPresent()) {
            ComponentMeta meta = optionalMeta.get();
            for(ClassMeta cm : meta.getControllers()) {
                if(cm.getName().equalsIgnoreCase(className)) {
                    return true;
                }
            }
            ClassMeta baseRepoMeta = new ClassMeta();
            baseRepoMeta.setName(className);
            meta.getControllers().add(baseRepoMeta);
            dodoRepository.save(meta);
            return false;
        }
        return false;
    }

    private IntermediateData processBaseRepository(IntermediateData id, Component component) {

        ComponentsClass cc = new ComponentsClass();
        cc.setName("BaseRepository");
        cc.setComponentName(component.getName());
        cc.setPackageName(COM_EXAMPLE.concat(id.getComponentName().toLowerCase()).concat(".repository;"));
        cc.setEntityPath(COM_EXAMPLE.concat(id.getComponentName().toLowerCase()).concat(".entity.*;"));
        cc.setOverride(component.isOverride());
        if(component.isAsync()){
            cc.setClassType(ClassTypes.quarkus_base_reactive_repository);
        } else {
            cc.setClassType(ClassTypes.quarkus_base_repository);
        }

        id.getRepositoryClasses().add(cc);
        return id;

    }



    private IntermediateData getExistingData(List<IntermediateData> idList, Component component) {

        for(IntermediateData existingId: idList) {
            if(existingId.getComponentName().equalsIgnoreCase(component.getName())) {
                return existingId;
            }
        }
        IntermediateData id = new IntermediateData();
        id.setComponentName(component.getName());
        setOtherProperties(id, Collections.singletonList(component));
        idList.add(id);
        return id;
    }


    @Override
    public void processRepository(IntermediateData id, APIObject apiObject, String methodName, String repositoryName) throws Exception {
        processBasicRepository(id, apiObject, methodName, repositoryName);
    }
}
