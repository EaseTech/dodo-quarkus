package [=package];

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.client.reactive.ReactiveRestClients;
import org.springframework.data.elasticsearch.config.AbstractReactiveElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;

@Configuration
@EnableReactiveElasticsearchRepositories
public class InfraESConfiguration extends AbstractReactiveElasticsearchConfiguration {

    @Value("${elasticsearch.hostname}")
    private String hostName;
    @Value("${elasticsearch.port}")
    private int port;
    @Value("${elasticsearch.username}")
    private String username;
    @Value("${elasticsearch.password}")
    private String password;


    @Override
    public ReactiveElasticsearchClient reactiveElasticsearchClient() {
        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
            .connectedTo(hostName.concat(":").concat(String.valueOf(port)))
            .withBasicAuth(username, password)
            .build();
        return ReactiveRestClients.create(clientConfiguration);
    }

    /**
    * You can set all these options in the Client Configuration
    * HttpHeaders httpHeaders = new HttpHeaders();
    * httpHeaders.add("some-header", "on every request")
    *
    * ClientConfiguration clientConfiguration = ClientConfiguration.builder()
    *   .connectedTo("localhost:9200", "localhost:9291")
    *   .usingSsl()
    *   .withProxy("localhost:8888")
    *   .withPathPrefix("ela")
    *   .withConnectTimeout(Duration.ofSeconds(5))
    *   .withSocketTimeout(Duration.ofSeconds(3))
    *   .withDefaultHeaders(defaultHeaders)
    *   .withBasicAuth(username, password)
    *   .withHeaders(() -> {
    *     HttpHeaders headers = new HttpHeaders();
    *     headers.add("currentTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    *     return headers;
    *   })
    *   .withWebClientConfigurer(webClient -> {
    *     //...
    *     return webClient;
    *   })
    *   .withHttpClientConfigurer(clientBuilder -> {
    *       //...
    *       return clientBuilder;
    *   })
    *   . // ... other options
    *   .build();
    */

}

