package com.miany.psycheecho.service;

import com.miany.psycheecho.content.EchoCategory;
import com.miany.psycheecho.content.EchoNote;
import com.miany.psycheecho.dto.response.StatisticsDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EchoService {

    Flux<EchoCategory> getAllCategories();

    Mono<EchoCategory> getCategoryByName(String name);

    Mono<EchoCategory> createCategory(EchoCategory category);

    Mono<EchoCategory> updateCategory(String name, EchoCategory category);

    Mono<Void> deleteCategory(String name);

    Flux<EchoNote> getEchoes(String categoryId);

    Mono<EchoNote> getEchoByName(String name);

    Mono<EchoNote> createEcho(EchoNote echo);

    Mono<EchoNote> updateEcho(String name, EchoNote echo);

    Mono<Void> deleteEcho(String name);

    void initializeDefaultCategories();

    Mono<StatisticsDTO> getStatistics();
}
