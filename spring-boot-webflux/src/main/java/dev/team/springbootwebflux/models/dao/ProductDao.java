package dev.team.springbootwebflux.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import dev.team.springbootwebflux.models.documents.Product;

public interface ProductDao extends ReactiveMongoRepository<Product, String> {

}
