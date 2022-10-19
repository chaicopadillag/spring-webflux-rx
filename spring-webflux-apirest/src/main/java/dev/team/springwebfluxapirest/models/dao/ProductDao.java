package dev.team.springwebfluxapirest.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import dev.team.springwebfluxapirest.models.documents.Product;
import reactor.core.publisher.Mono;

public interface ProductDao extends ReactiveMongoRepository<Product, String> {

    public Mono<Product> findByName(String name);

}
