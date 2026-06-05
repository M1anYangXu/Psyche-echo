package com.miany.psycheecho.endpoint;

import com.miany.psycheecho.content.EchoCategory;
import com.miany.psycheecho.content.EchoNote;
import com.miany.psycheecho.dto.response.ApiResponse;
import com.miany.psycheecho.service.EchoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Slf4j
@Component
@RequiredArgsConstructor
public class EchoEndpoint implements CustomEndpoint {

    private final EchoService echoService;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return route(GET("/echo/categories"), this::listCategories)
            .andRoute(GET("/echo/categories/{name}"), this::getCategory)
            .andRoute(POST("/echo/categories"), this::createCategory)
            .andRoute(PUT("/echo/categories/{name}"), this::updateCategory)
            .andRoute(DELETE("/echo/categories/{name}"), this::deleteCategory)
            .andRoute(GET("/echo/notes"), this::listNotes)
            .andRoute(GET("/echo/notes/{name}"), this::getNote)
            .andRoute(POST("/echo/notes"), this::createNote)
            .andRoute(PUT("/echo/notes/{name}"), this::updateNote)
            .andRoute(DELETE("/echo/notes/{name}"), this::deleteNote)
            .andRoute(GET("/echo/statistics"), this::getStatistics);
    }

    private Mono<ServerResponse> listCategories(org.springframework.web.reactive.function.server.ServerRequest request) {
        log.info("获取所有分类");
        return echoService.getAllCategories()
            .collectList()
            .flatMap(categories -> ServerResponse.ok().bodyValue(ApiResponse.success(categories)));
    }

    private Mono<ServerResponse> getCategory(org.springframework.web.reactive.function.server.ServerRequest request) {
        String name = request.pathVariable("name");
        log.info(String.format("获取分类: %s", name));
        return echoService.getCategoryByName(name)
            .flatMap(category -> ServerResponse.ok().bodyValue(ApiResponse.success(category)))
            .onErrorResume(e -> ServerResponse.badRequest().bodyValue(ApiResponse.error(400, e.getMessage())));
    }

    private Mono<ServerResponse> createCategory(org.springframework.web.reactive.function.server.ServerRequest request) {
        return request.bodyToMono(EchoCategory.class)
            .doOnNext(category -> log.info(String.format("创建分类: %s", category.getSpec().getName())))
            .flatMap(echoService::createCategory)
            .flatMap(created -> ServerResponse.ok().bodyValue(ApiResponse.success("创建成功", created)))
            .onErrorResume(e -> ServerResponse.badRequest().bodyValue(ApiResponse.error(400, e.getMessage())));
    }

    private Mono<ServerResponse> updateCategory(org.springframework.web.reactive.function.server.ServerRequest request) {
        String name = request.pathVariable("name");
        log.info(String.format("更新分类: %s", name));
        return request.bodyToMono(EchoCategory.class)
            .flatMap(category -> echoService.updateCategory(name, category))
            .flatMap(updated -> ServerResponse.ok().bodyValue(ApiResponse.success("更新成功", updated)))
            .onErrorResume(e -> ServerResponse.badRequest().bodyValue(ApiResponse.error(400, e.getMessage())));
    }

    private Mono<ServerResponse> deleteCategory(org.springframework.web.reactive.function.server.ServerRequest request) {
        String name = request.pathVariable("name");
        log.info(String.format("删除分类: %s", name));
        return echoService.deleteCategory(name)
            .then(ServerResponse.ok().bodyValue(ApiResponse.success("删除成功", null)))
            .onErrorResume(e -> ServerResponse.badRequest().bodyValue(ApiResponse.error(400, e.getMessage())));
    }

    private Mono<ServerResponse> listNotes(org.springframework.web.reactive.function.server.ServerRequest request) {
        String categoryId = request.queryParam("categoryId").orElse(null);
        log.info(String.format("获取日记列表, 分类: %s", categoryId));
        return echoService.getEchoes(categoryId)
            .collectList()
            .flatMap(echoes -> ServerResponse.ok().bodyValue(ApiResponse.success(echoes)));
    }

    private Mono<ServerResponse> getNote(org.springframework.web.reactive.function.server.ServerRequest request) {
        String name = request.pathVariable("name");
        log.info(String.format("获取日记: %s", name));
        return echoService.getEchoByName(name)
            .flatMap(echo -> ServerResponse.ok().bodyValue(ApiResponse.success(echo)))
            .onErrorResume(e -> ServerResponse.badRequest().bodyValue(ApiResponse.error(400, e.getMessage())));
    }

    private Mono<ServerResponse> createNote(org.springframework.web.reactive.function.server.ServerRequest request) {
        log.info("创建日记");
        return request.bodyToMono(EchoNote.class)
            .flatMap(echoService::createEcho)
            .flatMap(created -> ServerResponse.ok().bodyValue(ApiResponse.success("发布成功", created)))
            .onErrorResume(e -> ServerResponse.badRequest().bodyValue(ApiResponse.error(400, e.getMessage())));
    }

    private Mono<ServerResponse> updateNote(org.springframework.web.reactive.function.server.ServerRequest request) {
        String name = request.pathVariable("name");
        log.info(String.format("更新日记: %s", name));
        return request.bodyToMono(EchoNote.class)
            .flatMap(echo -> echoService.updateEcho(name, echo))
            .flatMap(updated -> ServerResponse.ok().bodyValue(ApiResponse.success("更新成功", updated)))
            .onErrorResume(e -> ServerResponse.badRequest().bodyValue(ApiResponse.error(400, e.getMessage())));
    }

    private Mono<ServerResponse> deleteNote(org.springframework.web.reactive.function.server.ServerRequest request) {
        String name = request.pathVariable("name");
        log.info(String.format("删除日记: %s", name));
        return echoService.deleteEcho(name)
            .then(ServerResponse.ok().bodyValue(ApiResponse.success("删除成功", null)))
            .onErrorResume(e -> ServerResponse.badRequest().bodyValue(ApiResponse.error(400, e.getMessage())));
    }

    private Mono<ServerResponse> getStatistics(org.springframework.web.reactive.function.server.ServerRequest request) {
        log.info("获取统计数据");
        return echoService.getStatistics()
            .flatMap(statistics -> ServerResponse.ok().bodyValue(ApiResponse.success(statistics)))
            .onErrorResume(e -> ServerResponse.badRequest().bodyValue(ApiResponse.error(400, e.getMessage())));
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("api.echo.miany.run/v1alpha1");
    }
}