package dev.team.springbootwebflux.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.team.springbootwebflux.models.documents.Product;
import dev.team.springbootwebflux.models.services.ProductService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/products")
public class ProductRestController {

	@Autowired
	private ProductService service;

	private static final Logger log = LoggerFactory.getLogger(ProductRestController.class);

	@GetMapping
	public Flux<Product> index() {

		Flux<Product> productos = service.findAll()
				.map(producto -> {
					producto.setName(producto.getName().toUpperCase());
					return producto;
				})
				.doOnNext(prod -> log.info(prod.getName()));

		return productos;
	}

	@GetMapping("/{id}")
	public Mono<Product> show(@PathVariable String id) {

		/* Mono<Producto> producto = dao.findById(id); */

		Flux<Product> productos = service.findAll();

		Mono<Product> producto = productos
				.filter(p -> p.getId().equals(id))
				.next()
				.doOnNext(prod -> log.info(prod.getName()));

		return producto;
	}

}
