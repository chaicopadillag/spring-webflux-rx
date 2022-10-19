package dev.team.springwebfluxclient.models.services;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import dev.team.springwebfluxclient.models.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductImp implements ProductService {

    @Autowired
    private WebClient.Builder client;

    @Override
    public Flux<Product> findAll() {
        return client.build().get().accept(APPLICATION_JSON_UTF8)
                .exchange()
                .flatMapMany(response -> response.bodyToFlux(Product.class));
    }

    @Override
    public Mono<Product> findById(String id) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        return client.build().get().uri("/{id}", params)
                .accept(APPLICATION_JSON_UTF8)
                .retrieve()
                .bodyToMono(Product.class);
        // .exchange()
        // .flatMap(response -> response.bodyToMono(Producto.class));
    }

    @Override
    public Mono<Product> save(Product producto) {
        return client.build().post()
                .accept(APPLICATION_JSON_UTF8)
                .contentType(APPLICATION_JSON_UTF8)
                // .body(fromObject(producto))
                .syncBody(producto)
                .retrieve()
                .bodyToMono(Product.class);
    }

    @Override
    public Mono<Product> update(Product producto, String id) {

        return client.build().put()
                .uri("/{id}", Collections.singletonMap("id", id))
                .accept(APPLICATION_JSON_UTF8)
                .contentType(APPLICATION_JSON_UTF8)
                .syncBody(producto)
                .retrieve()
                .bodyToMono(Product.class);
    }

    @Override
    public Mono<Void> delete(String id) {
        return client.build().delete().uri("/{id}", Collections.singletonMap("id", id))
                .retrieve()
                .bodyToMono(Void.class);
    }

    @Override
    public Mono<Product> upload(FilePart file, String id) {
        MultipartBodyBuilder parts = new MultipartBodyBuilder();
        parts.asyncPart("file", file.content(), DataBuffer.class).headers(h -> {
            h.setContentDispositionFormData("file", file.filename());
        });

        return client.build().post()
                .uri("/upload/{id}", Collections.singletonMap("id", id))
                .contentType(MULTIPART_FORM_DATA)
                .syncBody(parts.build())
                .retrieve()
                .bodyToMono(Product.class);
    }
}
