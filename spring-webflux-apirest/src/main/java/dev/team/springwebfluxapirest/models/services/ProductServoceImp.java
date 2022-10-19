package dev.team.springwebfluxapirest.models.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.team.springwebfluxapirest.models.dao.CategoryDao;
import dev.team.springwebfluxapirest.models.dao.ProductDao;
import dev.team.springwebfluxapirest.models.documents.Category;
import dev.team.springwebfluxapirest.models.documents.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductServoceImp implements ProductService {

    @Autowired
    private ProductDao dao;

    @Autowired
    private CategoryDao catDao;

    @Override
    public Flux<Product> findAll() {

        return dao.findAll();
    }

    @Override
    public Mono<Product> findById(String id) {

        return dao.findById(id);
    }

    @Override
    public Mono<Product> save(Product product) {
        return dao.save(product);
    }

    @Override
    public Mono<Void> delete(Product product) {

        return dao.delete(product);
    }

    @Override
    public Flux<Product> findAllWithNameUpperCase() {

        return dao.findAll().map(product -> {

            product.setName(product.getName().toUpperCase());
            return product;
        });
    }

    @Override
    public Flux<Product> findAllWithNameUpperCaseRepeat() {
        return findAllWithNameUpperCase().repeat(5000);
    }

    @Override
    public Flux<Category> findAllCategoria() {

        return catDao.findAll();
    }

    @Override
    public Mono<Category> findCategoriaById(String id) {

        return catDao.findById(id);
    }

    @Override
    public Mono<Category> saveCategoria(Category category) {

        return catDao.save(category);
    }

    @Override
    public Mono<Product> findByName(String name) {

        return dao.findByName(name);
    }

    @Override
    public Mono<Category> findCategoryByName(String name) {

        return catDao.findByName(name);
    }

}
