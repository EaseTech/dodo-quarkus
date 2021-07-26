package com.example.dodoquarqus;

import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableScheduling
@EnableElasticsearchRepositories
public class DodoQuarqusApplication {

    @Autowired
    CloseableHttpClient httpClient;

    public static void main(String[] args) {
        SpringApplication.run(DodoQuarqusApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());
        return restTemplate;
    }

    @Bean
    @Qualifier("springDependencies")
    public Map<String,String> springDependencies() {
        //devtools,lombok,configuration-processor,
        // web,webflux,data-rest,session,jersey,security,data-jpa,data-mongodb,data-elasticsearch
        Map<String, String> d = new HashMap<>();
        d.put("async", "webflux");
        d.put("web", "web");
        d.put("lombok", "lombok");
        d.put("config-processor", "configuration-processor");
        d.put("sql", "data-jpa");
        d.put("postgres", "data-jpa");
        d.put("mysql", "data-jpa");
        d.put("mongo", "data-mongodb");
        d.put("es", "data-elasticsearch");
        d.put("elasticsearch", "data-elasticsearch");
        d.put("jwt", "oauth2-resource-server");
//        d.put("reactive-es", "data-elasticsearch");
        d.put("reactive_es", "data-elasticsearch");

        return d;
    }

    @Bean
    @Qualifier("quarkusDependencies")
    public Map<String,String> quarqusDependencies() {
        Map<String, String> d = new HashMap<>();
        d.put("web", "e=resteasy");
        d.put("es", "e=elasticsearch-rest-client");
        d.put("jwt", "e=smallrye-jwt");
        d.put("async", "e=resteasy-reactive");
        d.put("kubernetes", "e=kubernetes");
        d.put("jib", "e=container-image-jib");
        return d;
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setHttpClient(httpClient);
        return clientHttpRequestFactory;
    }

}
