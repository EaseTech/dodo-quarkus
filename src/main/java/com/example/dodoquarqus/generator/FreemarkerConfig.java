package com.example.dodoquarqus.generator;

import com.example.dodoquarqus.comment.APIObject;
import com.example.dodoquarqus.comment.Items;
import com.google.common.base.CaseFormat;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

import static com.example.dodoquarqus.processor.ProcessorUtil.isCollectionProperty;
import static freemarker.template.Configuration.SQUARE_BRACKET_INTERPOLATION_SYNTAX;

public class FreemarkerConfig {

    public String BASE_WORKING_DIR = "/src/main/java/com/example/";

    public Configuration cfg = null;

    public FreemarkerConfig(String basePackagePath) {
        cfg = new Configuration(new Version(2, 3, 20));
        cfg.setClassForTemplateLoading(FreemarkerConfig.class, basePackagePath);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setInterpolationSyntax(SQUARE_BRACKET_INTERPOLATION_SYNTAX );
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    public String getControllerClassName(APIObject apiObject) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, getClassName(apiObject.getRequest().getPath())).concat("Controller");
    }



    public String getServiceClassName(APIObject apiObject) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, getClassName(apiObject.getRequest().getPath())).concat("Service");

    }

    public String getRepositoryClassName(APIObject apiObject) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, getEntityType(apiObject)).concat("Repository");
    }

    public String getClassName(String path) {
        return path.split("/")[1];
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

}
