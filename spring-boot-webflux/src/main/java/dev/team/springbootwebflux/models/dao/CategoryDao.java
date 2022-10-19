package dev.team.springbootwebflux.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import dev.team.springbootwebflux.models.documents.Category;

public interface CategoryDao extends ReactiveMongoRepository<Category, String> {

}
