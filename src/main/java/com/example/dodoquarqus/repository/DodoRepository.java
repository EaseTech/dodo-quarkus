package com.example.dodoquarqus.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface DodoRepository extends ElasticsearchRepository<ComponentMeta, String> {
}
