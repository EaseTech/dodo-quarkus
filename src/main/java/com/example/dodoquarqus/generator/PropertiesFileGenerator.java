package com.example.dodoquarqus.generator;

import com.example.dodoquarqus.comment.ClassTypes;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;


@Getter
@Setter
public class PropertiesFileGenerator extends Generator{

    public PropertiesFileGenerator(String basePath) {
        super(basePath);
    }

    @Autowired
    @Qualifier("quarqusDependencies")
    private Map<String, String> quarqusDependencies;

    @Autowired
    @Qualifier("springDependencies")
    private Map<String, String> springDependencies;

    private final String RESOURCES_PATH = "/src/main/resources/";
    private final String TEST_RESOURCES_PATH = "/src/test/resources/";
    private final String TEST_JAVA_PATH = "/src/test/java/";

    private File createDir(com.example.dodoquarqus.comment.Component component) {
        File dir = new File(getComponentPath(component).concat(RESOURCES_PATH));
        if(! dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }

    private File createTestDir(com.example.dodoquarqus.comment.Component component) {
        File dir = new File(getComponentPath(component).concat(TEST_RESOURCES_PATH));
        if(! dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }

    private File createTestJavaDir(com.example.dodoquarqus.comment.Component component, String path) {
        File dir = new File(getComponentPath(component).concat(TEST_JAVA_PATH).concat(path));
        if(! dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }
    private void writeToFile(com.example.dodoquarqus.comment.Component component, String templateName, Map<String, Object> inputData) throws IOException, TemplateException {

        Template template = cfg.getTemplate(templateName);
        Writer fileWriter = new FileWriter(new File(createDir(component), "application.properties"));


        template.process(inputData , fileWriter);
    }

    private Map<String, Object> getESInputData(com.example.dodoquarqus.comment.Component component) {
        int port = ThreadLocalRandom.current().nextInt(8000, 10000 + 1);
        Map<String, Object> input = new HashMap<>();
        input.put("componentName", component.getName());
        input.put("port", String.valueOf(port));
        input.put("esHostName", "localhost");
        input.put("esPort", "9200");
        input.put("esUserName", "");
        input.put("esPassword", "");
        return input;
    }

    private Map<String, Object> getInputData(com.example.dodoquarqus.comment.Component component, ClassTypes ct) {

        if(ct.equals(ClassTypes.es_properties)) {
            return getESInputData(component);
        }
        return new HashMap<>();
    }

    /**
     * spring.application.name=[=componentName]
     * server.port=[=port]
     * elasticsearch.hostname=[=esHostName]
     * elasticsearch.port=[=esPort]
     * elasticsearch.username=[=esUserName]
     * elasticsearch.password=[=esPassword]
     * management.endpoints.web.exposure.include=health,info,prometheus
     * @param components
     */
    public void generate(List<com.example.dodoquarqus.comment.Component> components){

        for(com.example.dodoquarqus.comment.Component component: components) {
            Map<String, Object> inputData = new HashMap<>();
            List<String> properties = new ArrayList<>();
            if(component.getEnableJWT().equalsIgnoreCase("true")) {
                addSecurityProperties(properties);

            }
            if(component.getType().equalsIgnoreCase("spring")){
                addSpringProperties(component, properties);

                inputData.put("properties", properties);

            }else if(component.getType().equalsIgnoreCase("quarkus")){
                addQuarqusProperties(component, properties);
                inputData.put("properties", properties);
            }
            try{
                writeToFile(component, "properties.ftl", inputData);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addQuarqusProperties(com.example.dodoquarqus.comment.Component component, List<String> properties) {
        int port = ThreadLocalRandom.current().nextInt(8000, 10000 + 1);
        String quarqusPort = "quarkus.http.port=".concat(String.valueOf(port));
        String appName = "quarkus.application.name=".concat(component.getName());
        properties.add(quarqusPort);
        properties.add(appName);
        if(component.getEnableJWT().equalsIgnoreCase("true")) {
            String comment = "# The JWT specification defines various levels of security of JWTs that one can use. The MicroProfile JWT RBAC specification requires that JWTs that are signed with the RSA-256 signature algorithm. " +
                    "This in turn requires a RSA public key pair. On the REST endpoint server side, you need to configure the location of the RSA public key to use to verify the JWT sent along with requests. " +
                    "The mp.jwt.verify.publickey.location=publicKey.pem setting configured below expects that the public key is available on the classpath as publicKey.pem.";
            String publicKeyLocation = "mp.jwt.verify.publickey.location=publicKey.pem";
            String issuer = "mp.jwt.verify.issuer=https://example.com/issuer";
            String nativeExe = "quarkus.native.resources.includes=publicKey.pem";
            properties.add(comment);
            properties.add(publicKeyLocation);
            properties.add(issuer);
            properties.add(nativeExe);
            createPublicPemFile(component);
            createPrivatePemFile(component);
            createJWTGeneratorClass(component);
        }
        if(component.getBackend().getType().equalsIgnoreCase("es")) {
            /**
             * quarkus.elasticsearch.hosts=
             * quarkus.elasticsearch.username=
             * quarkus.elasticsearch.password=
             */
            String esHost = "quarkus.elasticsearch.hosts=localhost:9200";
            String esUserName = "quarkus.elasticsearch.username=";
            String esPassword = "quarkus.elasticsearch.password=";
            properties.add(esHost);
            properties.add(esUserName);
            properties.add(esPassword);
        }
    }

    private void createPublicPemFile(com.example.dodoquarqus.comment.Component component){

        Map<String, Object> inputData = new HashMap<>();
        String publicKey = "-----BEGIN PUBLIC KEY-----\n" +
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlivFI8qB4D0y2jy0CfEq\n" +
                "Fyy46R0o7S8TKpsx5xbHKoU1VWg6QkQm+ntyIv1p4kE1sPEQO73+HY8+Bzs75XwR\n" +
                "TYL1BmR1w8J5hmjVWjc6R2BTBGAYRPFRhor3kpM6ni2SPmNNhurEAHw7TaqszP5e\n" +
                "UF/F9+KEBWkwVta+PZ37bwqSE4sCb1soZFrVz/UT/LF4tYpuVYt3YbqToZ3pZOZ9\n" +
                "AX2o1GCG3xwOjkc4x0W7ezbQZdC9iftPxVHR8irOijJRRjcPDtA6vPKpzLl6CyYn\n" +
                "sIYPd99ltwxTHjr3npfv/3Lw50bAkbT4HeLFxTx4flEoZLKO/g0bAoV2uqBhkA9x\n" +
                "nQIDAQAB\n" +
                "-----END PUBLIC KEY-----";
        inputData.put("publickey", publicKey);
        try {
            Template template = cfg.getTemplate("publicpem.ftl");
            Writer fileWriter = new FileWriter(new File(createDir(component), "publicKey.pem"));
            template.process(inputData , fileWriter);
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void createPrivatePemFile(com.example.dodoquarqus.comment.Component component) {
        Map<String, Object> inputData = new HashMap<>();
        String privateKey =
                "-----BEGIN PRIVATE KEY-----\n" +
                "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCWK8UjyoHgPTLa\n" +
                "PLQJ8SoXLLjpHSjtLxMqmzHnFscqhTVVaDpCRCb6e3Ii/WniQTWw8RA7vf4djz4H\n" +
                "OzvlfBFNgvUGZHXDwnmGaNVaNzpHYFMEYBhE8VGGiveSkzqeLZI+Y02G6sQAfDtN\n" +
                "qqzM/l5QX8X34oQFaTBW1r49nftvCpITiwJvWyhkWtXP9RP8sXi1im5Vi3dhupOh\n" +
                "nelk5n0BfajUYIbfHA6ORzjHRbt7NtBl0L2J+0/FUdHyKs6KMlFGNw8O0Dq88qnM\n" +
                "uXoLJiewhg9332W3DFMeOveel+//cvDnRsCRtPgd4sXFPHh+UShkso7+DRsChXa6\n" +
                "oGGQD3GdAgMBAAECggEAAjfTSZwMHwvIXIDZB+yP+pemg4ryt84iMlbofclQV8hv\n" +
                "6TsI4UGwcbKxFOM5VSYxbNOisb80qasb929gixsyBjsQ8284bhPJR7r0q8h1C+jY\n" +
                "URA6S4pk8d/LmFakXwG9Tz6YPo3pJziuh48lzkFTk0xW2Dp4SLwtAptZY/+ZXyJ6\n" +
                "96QXDrZKSSM99Jh9s7a0ST66WoxSS0UC51ak+Keb0KJ1jz4bIJ2C3r4rYlSu4hHB\n" +
                "Y73GfkWORtQuyUDa9yDOem0/z0nr6pp+pBSXPLHADsqvZiIhxD/O0Xk5I6/zVHB3\n" +
                "zuoQqLERk0WvA8FXz2o8AYwcQRY2g30eX9kU4uDQAQKBgQDmf7KGImUGitsEPepF\n" +
                "KH5yLWYWqghHx6wfV+fdbBxoqn9WlwcQ7JbynIiVx8MX8/1lLCCe8v41ypu/eLtP\n" +
                "iY1ev2IKdrUStvYRSsFigRkuPHUo1ajsGHQd+ucTDf58mn7kRLW1JGMeGxo/t32B\n" +
                "m96Af6AiPWPEJuVfgGV0iwg+HQKBgQCmyPzL9M2rhYZn1AozRUguvlpmJHU2DpqS\n" +
                "34Q+7x2Ghf7MgBUhqE0t3FAOxEC7IYBwHmeYOvFR8ZkVRKNF4gbnF9RtLdz0DMEG\n" +
                "5qsMnvJUSQbNB1yVjUCnDAtElqiFRlQ/k0LgYkjKDY7LfciZl9uJRl0OSYeX/qG2\n" +
                "tRW09tOpgQKBgBSGkpM3RN/MRayfBtmZvYjVWh3yjkI2GbHA1jj1g6IebLB9SnfL\n" +
                "WbXJErCj1U+wvoPf5hfBc7m+jRgD3Eo86YXibQyZfY5pFIh9q7Ll5CQl5hj4zc4Y\n" +
                "b16sFR+xQ1Q9Pcd+BuBWmSz5JOE/qcF869dthgkGhnfVLt/OQzqZluZRAoGAXQ09\n" +
                "nT0TkmKIvlza5Af/YbTqEpq8mlBDhTYXPlWCD4+qvMWpBII1rSSBtftgcgca9XLB\n" +
                "MXmRMbqtQeRtg4u7dishZVh1MeP7vbHsNLppUQT9Ol6lFPsd2xUpJDc6BkFat62d\n" +
                "Xjr3iWNPC9E9nhPPdCNBv7reX7q81obpeXFMXgECgYEAmk2Qlus3OV0tfoNRqNpe\n" +
                "Mb0teduf2+h3xaI1XDIzPVtZF35ELY/RkAHlmWRT4PCdR0zXDidE67L6XdJyecSt\n" +
                "FdOUH8z5qUraVVebRFvJqf/oGsXc4+ex1ZKUTbY0wqY1y9E39yvB3MaTmZFuuqk8\n" +
                "f3cg+fr8aou7pr9SHhJlZCU=\n" +
                "-----END PRIVATE KEY-----";
        inputData.put("privatekey", privateKey);
        try {
            Template template = cfg.getTemplate("privatepem.ftl");
            Writer fileWriter = new FileWriter(new File(createTestDir(component), "privateKey.pem"));
            template.process(inputData , fileWriter);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createJWTGeneratorClass(com.example.dodoquarqus.comment.Component component) {
        Map<String, Object> inputData = new HashMap<>();
        try {
            Template template = cfg.getTemplate("JWTGenerator.ftl");
            Writer fileWriter = new FileWriter(new File(createTestJavaDir(component, "com/example/jwt/"), "GenerateToken.java"));
            template.process(inputData , fileWriter);
        }catch (Exception e){
            e.printStackTrace();
        }
//        createTestJavaDir(component);
    }



    private void addSpringProperties(com.example.dodoquarqus.comment.Component component, List<String> properties) {
        String appName = "spring.application.name=".concat(component.getName());
        int port = ThreadLocalRandom.current().nextInt(8000, 10000 + 1);
        String serverPort = "server.port=".concat(String.valueOf(port));
        String managementEndpoints = "management.endpoints.web.exposure.include=health,info,prometheus";
        properties.add(appName);
        properties.add(serverPort);
        properties.add(managementEndpoints);

        if(component.getBackend().getType().equalsIgnoreCase("es") || component.getBackend().getType().equalsIgnoreCase("reactive_es") || component.getBackend().getType().equalsIgnoreCase("reactive-es")) {
            String esHost = "elasticsearch.hostname=".concat("localhost");
            String esPort = "elasticsearch.port=".concat("9200");
            String esUserName= "elasticsearch.username=".concat("");
            String esPassword= "elasticsearch.password=".concat("");
            properties.add(esHost);
            properties.add(esPort);
            properties.add(esUserName);
            properties.add(esPassword);
        }
    }

    private void addSecurityProperties(List<String> properties) {
        properties.add("spring.security.oauth2.client.provider.okta.issuer-uri = https://dev-7548112.okta.com/oauth2/default");
        properties.add("spring.security.oauth2.client.registration.okta.client-id = 0oa15bd0z9MW5nxK95d7");
        properties.add("spring.security.oauth2.client.registration.okta.client-secret = Wa_JlxPVvYMG_EN1couy9GxzBS3VdArQWRdYRpuU");
        properties.add("spring.security.oauth2.client.registration.okta.scope = openid");
    }


}
