package dev.team.springwebfluxapirest.models.services;

import dev.team.springwebfluxapirest.models.documents.Category;
import dev.team.springwebfluxapirest.models.documents.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {

    public Flux<Product> findAll();

    public Mono<Product> findById(String id);

    public Mono<Product> save(Product product);

    public Mono<Void> delete(Product product);

    public Flux<Product> findAllWithNameUpperCase();

    public Flux<Product> findAllWithNameUpperCaseRepeat();

    public Flux<Category> findAllCategoria();

    public Mono<Category> findCategoriaById(String id);

    public Mono<Category> saveCategoria(Category category);

    public Mono<Product> findByName(String name);

    public Mono<Category> findCategoryByName(String name);

}
