package com.example.dodoquarqus.comment;

import lombok.Getter;

@Getter
public enum ClassTypes {

    controller("/controller", "Controller.ftl", ".java"),
    json_util("/utility", "JsonUtils.ftl", "JsonUtils.java"),
    app_config("/", "ApplicationConfiguration.ftl", "ApplicationConfiguration.java"),
    sec_config_adapter("/security", "WebSecurityConfigurerAdapter.ftl", "WebSecurityConfigurerAdapter.java"),
    sec_config("/security", "SecurityConfiguration.ftl", "SecurityConfiguration.java"),
    auth_manager_resolver("/security", "RequestMatchingAuthenticationManagerResolver.ftl", "RequestMatchingAuthenticationManagerResolver.java"),
    quarkus_base_repository("/repository", "BaseRepository.ftl", ".java"),
    quarkus_base_reactive_repository("/repository", "BaseReactiveRepository.ftl", ".java"),
    controller_advisor("/exception", "ControllerAdvisor.ftl", "ControllerAdvisor.java"),
    controller_get("", "get.ftl", ""),
    service("/service", "Service.ftl", ".java"),
    repository("","",""),
    es_repository("/repository", "ESRepository.ftl", ".java"),
    es_custom_repository("/repository", "ESRepositoryCustom.ftl", ".java"),
    es_custom_impl_repository("/repository", "ESRepositoryCustomImpl.ftl", ".java"),
//    es_entity("/entity", "ESEntity.ftl", ".java"),
    entity("/entity", "Entity.ftl", ".java"),
    es_nested_entity("/entity", "PojoEntity.ftl", ".java"),
    http_config("/", "HttpClientConfig.ftl", "HttpClientConfig.java"),
    es_properties("", "ESProperties.ftl", "application.properties"),
    exception_class("/exception", "Exception.ftl", "NotFoundException.java"),
    exception_method("", "ExceptionMethod.ftl", ""),
    converter("/converter", "Converter.ftl", ".java"),
    base_converter("/converter", "BaseConverter.ftl", ".java"),
    base_entity("/entity", "BaseEntity.ftl", ".java"),
    pojo_entity("/entity","PojoEntity.ftl",".java"),
    es_application("/", "ESApplication.ftl", "ESConfiguration.java"),
    reactive_es_application("/", "ESReactiveApplication.ftl", "ESConfiguration.java");

    private String path;
    private String ftl;
    private String classNameSuffix;

    ClassTypes(String path, String ftl, String classNameSuffix) {
        this.path = path;
        this.ftl = ftl;
        this.classNameSuffix = classNameSuffix;
    }


}
