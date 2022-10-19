package dev.team.springwebfluxclient.handler;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import dev.team.springwebfluxclient.models.Product;
import dev.team.springwebfluxclient.models.services.ProductService;
import reactor.core.publisher.Mono;

@Component
public class ProductHandler {

    @Autowired
    private ProductService service;

    public Mono<ServerResponse> listar(ServerRequest request) {
        return ServerResponse.ok().contentType(APPLICATION_JSON_UTF8)
                .body(service.findAll(), Product.class);
    }

    public Mono<ServerResponse> ver(ServerRequest request) {
        String id = request.pathVariable("id");
        return errorHandler(
                service.findById(id).flatMap(p -> ServerResponse.ok()
                        .contentType(APPLICATION_JSON_UTF8)
                        .syncBody(p))
                        .switchIfEmpty(ServerResponse.notFound().build()));
    }

    public Mono<ServerResponse> crear(ServerRequest request) {
        Mono<Product> producto = request.bodyToMono(Product.class);

        return producto.flatMap(p -> {
            if (p.getCreatedAt() == null) {
                p.setCreatedAt(new Date());
            }
            return service.save(p);
        }).flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(p.getId())))
                .contentType(APPLICATION_JSON_UTF8)
                .syncBody(p))
                .onErrorResume(error -> {
                    WebClientResponseException errorResponse = (WebClientResponseException) error;
                    if (errorResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return ServerResponse.badRequest()
                                .contentType(APPLICATION_JSON_UTF8)
                                .syncBody(errorResponse.getResponseBodyAsString());
                    }
                    return Mono.error(errorResponse);
                });
    }

    public Mono<ServerResponse> editar(ServerRequest request) {
        Mono<Product> producto = request.bodyToMono(Product.class);
        String id = request.pathVariable("id");

        return errorHandler(producto
                .flatMap(p -> service.update(p, id))
                .flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(p.getId())))
                        .contentType(APPLICATION_JSON_UTF8)
                        .syncBody(p)));
    }

    public Mono<ServerResponse> eliminar(ServerRequest request) {
        String id = request.pathVariable("id");
        return errorHandler(
                service.delete(id).then(ServerResponse.noContent().build()));
    }

    public Mono<ServerResponse> upload(ServerRequest request) {
        String id = request.pathVariable("id");
        return errorHandler(
                request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
                        .cast(FilePart.class)
                        .flatMap(file -> service.upload(file, id))
                        .flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(p.getId())))
                                .contentType(APPLICATION_JSON_UTF8)
                                .syncBody(p)));
    }

    private Mono<ServerResponse> errorHandler(Mono<ServerResponse> response) {
        return response.onErrorResume(error -> {
            WebClientResponseException errorResponse = (WebClientResponseException) error;
            if (errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                Map<String, Object> body = new HashMap<>();
                body.put("error", "No existe el producto: ".concat(errorResponse.getMessage()));
                body.put("timestamp", new Date());
                body.put("status", errorResponse.getStatusCode().value());
                return ServerResponse.status(HttpStatus.NOT_FOUND).syncBody(body);
            }
            return Mono.error(errorResponse);
        });
    }

}
