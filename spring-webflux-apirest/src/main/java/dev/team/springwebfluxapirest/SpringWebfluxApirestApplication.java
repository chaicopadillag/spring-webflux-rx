package dev.team.springwebfluxapirest;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import dev.team.springwebfluxapirest.models.documents.Category;
import dev.team.springwebfluxapirest.models.documents.Product;
import dev.team.springwebfluxapirest.models.services.ProductService;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringWebfluxApirestApplication implements CommandLineRunner {
	@Autowired
	private ProductService service;

	@Autowired
	private ReactiveMongoTemplate mongoTemplate;

	private static final Logger log = LoggerFactory.getLogger(SpringWebfluxApirestApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringWebfluxApirestApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		mongoTemplate.dropCollection("products").subscribe();
		mongoTemplate.dropCollection("categories").subscribe();

		Category electronico = new Category("Electrónico");
		Category deporte = new Category("Deporte");
		Category computacion = new Category("Computación");
		Category muebles = new Category("Muebles");

		Flux.just(electronico, deporte, computacion, muebles)
				.flatMap(service::saveCategoria)
				.doOnNext(c -> {
					log.info("Categoria creada: " + c.getName() + ", Id: " + c.getId());
				}).thenMany(
						Flux.just(new Product("TV Panasonic Pantalla LCD", 456.89, electronico),
								new Product("Sony Camara HD Digital", 177.89, electronico),
								new Product("Apple iPod", 46.89, electronico),
								new Product("Sony Notebook", 846.89, computacion),
								new Product("Hewlett Packard Multifuncional", 200.89, computacion),
								new Product("Bianchi Bicicleta", 70.89, deporte),
								new Product("HP Notebook Omen 17", 2500.89, computacion),
								new Product("Mica Cómoda 5 Cajones", 150.89, muebles),
								new Product("TV Sony Bravia OLED 4K Ultra HD", 2255.89, electronico))
								.flatMap(producto -> {
									producto.setCreatedAt(new Date());
									return service.save(producto);
								}))
				.subscribe(producto -> log.info("Insert: " + producto.getId() + " " + producto.getName()));
	}

}
