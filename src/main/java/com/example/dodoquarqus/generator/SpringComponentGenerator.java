package com.example.dodoquarqus.generator;

import com.example.dodoquarqus.comment.ClassTypes;
import com.example.dodoquarqus.comment.ComponentsClass;
import com.google.common.base.CaseFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class SpringComponentGenerator extends ComponentGenerator{

    public SpringComponentGenerator(@Autowired RestTemplate restTemplate,
                                    @Autowired @Qualifier("springDependencies") Map<String, String> springDependencies) {

        super(restTemplate, springDependencies, "/template/spring/");
    }

    public void prepareComponent(com.example.dodoquarqus.comment.Component component) {
        super.prepareComponent(component);
        handleBackendAnnotations(component);
    }

    private void handleBackendAnnotations(com.example.dodoquarqus.comment.Component component) {
        addApplicationConfiguration(component);
        if(component.getBackend().getType().equalsIgnoreCase("es")) {
            addESRelatedCode(component);

        }else if(component.getBackend().getType().equalsIgnoreCase("reactive-es") || component.getBackend().getType().equalsIgnoreCase("reactive_es")) {
            addReactiveESRelatedCode(component);
        }
        if(component.getEnableJWT().equalsIgnoreCase("true")) {
            addSecurityClasses(component);
        }
    }

    private void addApplicationConfiguration(com.example.dodoquarqus.comment.Component component) {
        ComponentsClass cc = new ComponentsClass();
        cc.setComponentName(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, component.getName()));
        cc.setPackageName("com.example.".concat(component.getName().toLowerCase()));
        cc.setClassType(ClassTypes.app_config);
        cc.setName("");
        generate(cc);
    }

    private void addSecurityClasses(com.example.dodoquarqus.comment.Component component) {

        ComponentsClass cc = new ComponentsClass();
        cc.setComponentName(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, component.getName()));
        cc.setPackageName("com.example.".concat(component.getName().toLowerCase()).concat(".security"));
        cc.setClassType(ClassTypes.sec_config_adapter);
        cc.setName(cc.getComponentName());
        generate(cc);

        cc.setName("");
        cc.setClassType(ClassTypes.auth_manager_resolver);
        generate(cc);

        cc.setClassType(ClassTypes.sec_config);
        generate(cc);


    }

    private void addESRelatedCode(com.example.dodoquarqus.comment.Component component) {
        addHttpConfig(component);
        addESBeans(component);

    }

    private void addReactiveESRelatedCode(com.example.dodoquarqus.comment.Component component) {
        addHttpConfig(component);
        addReactiveESConfig(component);
    }

    private void addReactiveESConfig(com.example.dodoquarqus.comment.Component component) {
        String componentName = component.getName();
        ComponentsClass cc = new ComponentsClass();
        cc.setPackageName("com.example.".concat(componentName.toLowerCase()));
        cc.setComponentName(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, componentName));
        cc.setClassType(ClassTypes.reactive_es_application);
        cc.setName(cc.getComponentName());
//        cc.setOverride(true);
        cc.setJwt(component.getEnableJWT());
        generate(cc);
    }

    private void addHttpConfig(com.example.dodoquarqus.comment.Component component) {
        String componentName = component.getName();

        ComponentsClass cc = new ComponentsClass();
        cc.setPackageName("com.example.".concat(componentName.toLowerCase()));
        cc.setComponentName(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, componentName));
        cc.setClassType(ClassTypes.http_config);
        cc.setName(cc.getComponentName());
        generate(cc);
    }

    private void addESBeans(com.example.dodoquarqus.comment.Component component) {

        String componentName = component.getName();
        ComponentsClass cc = new ComponentsClass();
        cc.setPackageName("com.example.".concat(componentName.toLowerCase()));
        cc.setComponentName(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, componentName));
        cc.setClassType(ClassTypes.es_application);
        cc.setName(cc.getComponentName());
//        cc.setOverride(true);
        cc.setJwt(component.getEnableJWT());
        generate(cc);
    }
}
