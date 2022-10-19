package dev.team.springwebfluxapirest.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import dev.team.springwebfluxapirest.models.documents.Category;
import reactor.core.publisher.Mono;

public interface CategoryDao extends ReactiveMongoRepository<Category, String> {

    public Mono<Category> findByName(String name);

}
