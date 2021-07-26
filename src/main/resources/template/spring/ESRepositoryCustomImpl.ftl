package [=package];


import java.util.*;

import  [=entityPath];

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.*;

@Repository
public class [=entityType]RepositoryImpl implements [=entityType]RepositoryCustom {

<#if async == false>
    protected ElasticsearchOperations elasticsearchOperations;

    public [=entityType]RepositoryImpl(@Autowired ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }
    <#else>
    protected ReactiveElasticsearchOperations elasticsearchOperations;

    public [=entityType]RepositoryImpl(@Autowired ReactiveElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }
</#if>

/**
    public Flux<[=entityType]> findAll() {
        // Simulate the data streaming every 2 seconds.
        return Flux.interval(Duration.ofSeconds(2))
        .onBackpressureDrop()
        .map(interval -> get())
        .flatMapIterable(v -> v);
        }
*/


    [=methods]

}