package com.example.dodoquarqus.generator;

import com.example.dodoquarqus.comment.*;
import com.example.dodoquarqus.repository.DodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.example.dodoquarqus.processor.ProcessorUtil.getIdType;

@Service
public class QuarkusESEntityGenerator extends EntityGenerator {

    public QuarkusESEntityGenerator(@Autowired DodoRepository dodoRepository) {
        super("/template/quarkus/", dodoRepository);
    }

    public void generateQuarkusEntity(Component component){
        generatBaseEntity(component);
        generateEntity(component);
    }



    private void generatBaseEntity(Component component) {
        ComponentsClass cc = new ComponentsClass();
        cc.setClassType(ClassTypes.base_entity);
        cc.setName("BaseEntity");
        cc.setPackageName("com.example.".concat(component.getName().toLowerCase()).concat(".entity;"));
        cc.setComponentName(component.getName());
        generate(cc);
    }




}
