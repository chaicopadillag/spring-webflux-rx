package dev.team.springbootwebflux.models.services;

import dev.team.springbootwebflux.models.documents.Category;
import dev.team.springbootwebflux.models.documents.Product;
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

}
