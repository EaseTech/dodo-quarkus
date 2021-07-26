package [=package];

import [=entityPath];
import com.example.infra.utility.JsonUtils;
import io.netty.handler.codec.http.HttpMethod;
import lombok.Data;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.*;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.sniff.Sniffer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.jboss.logging.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
public abstract class BaseRepository<T extends BaseEntity> {



    @Inject
    Logger log;

    @Inject
    JsonUtils jsonUtils;

    @ConfigProperty(name = "quarkus.elasticsearch.hosts", defaultValue = "localhost:9200")
    String[] hosts;

    @ConfigProperty(name = "quarkus.elasticsearch.threads", defaultValue = "10")
    Optional<Integer> numThreads;

    @ConfigProperty(name = "quarkus.elasticsearch.username", defaultValue = "")
    Optional<String> username;

    @ConfigProperty(name = "quarkus.elasticsearch.password", defaultValue = "")
    Optional<String> password;

    @ConfigProperty(name = "quarkus.elasticsearch.num.shards", defaultValue = "3")
    Optional<Integer> numShards;

    @ConfigProperty(name = "quarkus.elasticsearch.num.replicas", defaultValue = "1")
    Optional<Integer> numReplicas;

    RestHighLevelClient restClient;
    Sniffer sniffer;

    @PostConstruct
    public void init() {
        log.info("init started");
        final CredentialsProvider credentialsProvider =
        new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
        new UsernamePasswordCredentials(username.orElse(""), password.orElse("")));
        List<HttpHost> httpHosts = Arrays.stream(hosts)
            .map(s -> StringUtils.split(s, ':'))
            .map(strings -> new HttpHost(strings[0], Integer.valueOf(strings[1])))
            .collect(Collectors.toList());
        RestClientBuilder builder = RestClient.builder(httpHosts.toArray(new HttpHost[httpHosts.size()]));
        getNumThreads().ifPresent(integer ->
            builder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultIOReactorConfig(
                IOReactorConfig
                    .custom()
                    .setIoThreadCount(integer)
            .build())
        ));
        builder.setHttpClientConfigCallback(httpAsyncClientBuilder -> httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider));

        restClient = new RestHighLevelClient(builder);
        log.info("Successfully created the Rest High Level Client instance.");

        sniffer = Sniffer.builder(getRestClient().getLowLevelClient()).build();
        indexExists()
            .doOnNext(exists -> log.info("index exists".concat( getIndex()).concat( exists.toString())))
            .filter(exists -> !exists)
            .flatMap(exists -> createIndex())
            .doOnNext(created -> log.info("index exists".concat( getIndex()).concat( created.toString())))
            .doOnTerminate(() -> log.info("init completed"))
            .block();
    }

    protected Mono<Boolean> indexExists() {
        return Mono.just(false)
            .flatMap(atomicBoolean -> Flux
            .<Boolean>create(fluxSink -> getRestClient().getLowLevelClient()
            .performRequestAsync(
                new Request(HttpMethod.HEAD.name(), getIndex()), new ResponseListener() {
                    @Override
                    public void onSuccess(Response response) {
                        logResponse(response);
                        boolean result = response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
                        fluxSink.next(result);
                        fluxSink.complete();
                    }
                    @Override
                    public void onFailure(Exception exception) {
                        log.error("unable to check for index", exception);
                        fluxSink.error(new RuntimeException(exception));
                    }
                }))
            .next()
        );
    }

    protected Mono<Boolean> createIndex() {
        return Mono.just(false).flatMap(atomicBoolean ->
            Flux.<Boolean>create(fluxSink -> getRestClient().indices().createAsync(
                new CreateIndexRequest(getIndex()).settings(Settings.builder()
                    .put("index.number_of_shards", String.valueOf(numShards.orElse(3)))
                    .put("index.number_of_replicas", String.valueOf(numReplicas.orElse(1)))
                    .build()),
                RequestOptions.DEFAULT, new ActionListener<>() {

                    @Override
                    public void onResponse(CreateIndexResponse createIndexResponse) {
                        log.debug("Index:".concat(createIndexResponse.index()).concat("ack status:").concat(String.valueOf(createIndexResponse.isAcknowledged())));
                        fluxSink.next(createIndexResponse.isAcknowledged());
                        fluxSink.complete();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        log.error("unable to create index: ".concat(getIndex()), e);
                        fluxSink.error(new RuntimeException(e));
                    }
                }
            )).next()
        );
    }

    public Mono<Boolean> deleteById(T data) {
        return Mono.just(data.getId())
            .filter(s -> StringUtils.isNotBlank(String.valueOf(s)))
            .flatMap(s -> Flux
                .<Boolean>create(fluxSink -> {
                    DeleteRequest request = new DeleteRequest(getIndex(), String.valueOf(s));
                    request.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
                    getRestClient().deleteAsync(request, RequestOptions.DEFAULT, new ActionListener<>() {
                        @Override
                        public void onResponse(DeleteResponse deleteResponse) {
                            log.debug("delete result:".concat(String.valueOf(deleteResponse)));
                            fluxSink.next(StringUtils.equalsAnyIgnoreCase("DELETED", deleteResponse.getResult().getLowercase()));
                            fluxSink.complete();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            log.error("unable to delete", e);
                            fluxSink.error(new RuntimeException(e));
                        }
                    });
                })
                .next()
            )
        .defaultIfEmpty(false);
    }

    public Mono<T> findById(T data) {
        return Mono.just(String.valueOf(data.getId()))
            .filter(s -> StringUtils.isNotBlank(s))
            .flatMap(s -> Flux
                .<T>create(fluxSink -> {
                    GetRequest request = new GetRequest(getIndex(), s);
                    getRestClient().getAsync(request, RequestOptions.DEFAULT, new ActionListener<>() {
                        @Override
                        public void onResponse(GetResponse response) {
                            log.debug("get result: ".concat(response.toString()));
                            if (response.isSourceEmpty()) {
                                fluxSink.complete();
                                return;
                            }
                            Map<String, Object> map = response.getSourceAsMap();
                            T result = (T) getJsonUtils().fromMap(map, data.getClass());
                            fluxSink.next(result);
                            fluxSink.complete();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            log.error("unable to get", e);
                            fluxSink.error(new RuntimeException(e));
                        }
                    });
                })
            .next()
        );
    }

    public Mono<T> save(T t) {

        return Mono.deferContextual(Mono::just)
        .flatMap(context -> {
            if (StringUtils.isBlank(String.valueOf(t.getId()))) {
                t.setId(UUID.randomUUID().toString());
            }

            IndexRequest request = new IndexRequest(getIndex());
            request.id(String.valueOf(t.getId()));
            request.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
            Mono<T> response =  Mono.just(getJsonUtils().toStringLazy(t).toString())
                .flatMapMany(s -> Flux.<T>create(fluxSink -> {
                    request.source(s, XContentType.JSON);
                    getRestClient().indexAsync(
                        request, RequestOptions.DEFAULT, new ActionListener<IndexResponse>() {
                            @Override
                            public void onResponse(IndexResponse response) {
                                log.debug("save result: ".concat(response.toString()));
                                fluxSink.next(t);
                                fluxSink.complete();
                            }
                            @Override
                            public void onFailure(Exception e) {
                                log.error("unable to save", e);
                                fluxSink.error(new RuntimeException(e));
                            }
                        });
                    })
                )
            .next();
            return response;
        });
    }

    protected Flux<T> findByMatch(Map<String, Object> map) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        return findBy(map, query,
            queryBuilder -> query.should(queryBuilder),
            (s, o) -> getSearchSourceBuilderByMatch(s, o));
    }

    protected Flux<T> findByMatch(String fieldName, Object value) {
        SearchRequest searchRequest = getSearchRequestByMatch(fieldName, value);
        return search(searchRequest);
    }

    protected Flux<T> findBy(Map<String, Object> map, BoolQueryBuilder query, Function<QueryBuilder, BoolQueryBuilder> fn, BiFunction<String, Object, SearchSourceBuilder> fn1 ) {
        SearchRequest searchRequest = new SearchRequest(getIndex());

        SearchSourceBuilder searchSourceBuilder1 = new SearchSourceBuilder();
        searchSourceBuilder1.query(query);

        searchRequest.source(searchSourceBuilder1);

        map.entrySet().stream()
            .filter(entry -> StringUtils.isNotBlank(entry.getKey()) && entry.getValue() != null)
            .map(entry -> fn1.apply(entry.getKey(), entry.getValue()))
            .map(searchSourceBuilder -> searchSourceBuilder.query())
            .forEach(queryBuilder -> fn.apply(queryBuilder));

        return search(searchRequest);
    }

    protected Flux<T> search(SearchRequest searchRequest) {
        return Flux.create(fluxSink -> getRestClient().searchAsync(
        searchRequest, RequestOptions.DEFAULT, searchResponseAction(fluxSink)));
    }

    protected ActionListener<SearchResponse> searchResponseAction(FluxSink<T> fluxSink) {
            return new ActionListener<SearchResponse>() {

                public void onResponse(SearchResponse searchResponse) {
                    SearchHits hits = searchResponse.getHits();
                    //TODO: Implement Metadata capture
                    //Optional.ofNullable(hits.getTotalHits())
                    //  .map(totalHits -> totalHits.value)
                //      .ifPresent(aLong -> metadata.setHits(aLong));
                Arrays.stream(hits.getHits()).forEach(fields -> {
                Map<String, Object> map = fields.getSourceAsMap();
                T t = getJsonUtils().fromMap(map, getEntityClass());
                    fluxSink.next(t);
                });
                    fluxSink.complete();
                }


                public void onFailure(Exception e) {
                    log.error("search failed", e);
                    fluxSink.error(new RuntimeException(e));
                }
            };

    }

    protected SearchRequest getSearchRequestByMatch(String fieldName, Object value) {
        SearchRequest searchRequest = new SearchRequest(getIndex());
        SearchSourceBuilder searchSourceBuilder = getSearchSourceBuilderByMatch(fieldName, value);
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }
    protected SearchSourceBuilder getSearchSourceBuilderByMatch(String fieldName, Object value) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery(fieldName, value));
        return searchSourceBuilder;
    }

    protected Flux<T> findByExactMatch(Map<String, Object> map) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        return findBy(map, query,
            queryBuilder -> query.must(queryBuilder),
            (s, o) -> getSearchSourceBuilderByExactMatch(s, o));
    }

    protected SearchRequest getSearchRequestByExactMatch(String fieldName, Object value) {
        SearchRequest searchRequest = new SearchRequest(getIndex());
        SearchSourceBuilder searchSourceBuilder = getSearchSourceBuilderByExactMatch(fieldName, value);
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }

    protected SearchSourceBuilder getSearchSourceBuilderByExactMatch(String fieldName, Object value) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery(fieldName, value));
        return searchSourceBuilder;
    }

    private void logResponse(Response response) {
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            log.debug("entity is null");
            return;
        }
        try {
            InputStream content = entity.getContent();
            log.debug("response result: {}".concat(IOUtils.toString(content, StandardCharsets.UTF_8)));
        } catch (Exception e) {
            log.error("unable to log response", e);
        }
    }
    @PreDestroy
    public void shutdown() {
        log.info("shutdown started");
        getSniffer().close();
        try {
            getRestClient().close();
        } catch (IOException e) {
            log.error("unable to close the rest client", e);
        }
        log.info("shutdown completed");
    }

    protected abstract String getIndex();

    protected abstract Class<T> getEntityClass();
}


