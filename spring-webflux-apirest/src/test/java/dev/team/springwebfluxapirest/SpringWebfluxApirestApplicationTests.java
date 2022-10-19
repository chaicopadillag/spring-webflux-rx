package dev.team.springwebfluxapirest;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.team.springwebfluxapirest.models.documents.Category;
import dev.team.springwebfluxapirest.models.documents.Product;
import dev.team.springwebfluxapirest.models.services.ProductService;
import reactor.core.publisher.Mono;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class SpringWebfluxApirestApplicationTests {

	@Autowired
	private WebTestClient client;

	@Autowired
	private ProductService service;

	@Value("${config.base.endpoint}")
	private String url;

	@Test
	public void listarTest() {

		client.get()
				.uri(url)
				.accept(MediaType.APPLICATION_JSON_UTF8)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
				.expectBodyList(Product.class)
				.consumeWith(response -> {
					List<Product> products = response.getResponseBody();
					products.forEach(p -> {
						System.out.println(p.getName());
					});

					Assertions.assertThat(products.size() > 0).isTrue();
				});
		// .hasSize(9);
	}

	@Test
	public void verTest() {

		Product producto = service.findByName("TV Panasonic Pantalla LCD").block();

		client.get()
				.uri(url + "/{id}", Collections.singletonMap("id", producto.getId()))
				.accept(MediaType.APPLICATION_JSON_UTF8)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
				.expectBody(Product.class)
				.consumeWith(response -> {
					Product p = response.getResponseBody();
					Assertions.assertThat(p.getId()).isNotEmpty();
					Assertions.assertThat(p.getId().length() > 0).isTrue();
					Assertions.assertThat(p.getName()).isEqualTo("TV Panasonic Pantalla LCD");
				});

		// .expectBody()
		// .jsonPath("$.id").isNotEmpty()
		// .jsonPath("$.name").isEqualTo("TV Panasonic Pantalla LCD");

	}

	@Test
	public void crearTest() {

		Category categoria = service.findCategoryByName("Muebles").block();

		Product product = new Product("Mesa comedor", 100.00, categoria);

		client.post().uri(url)
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.accept(MediaType.APPLICATION_JSON_UTF8)
				.body(Mono.just(product), Product.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
				.expectBody()
				.jsonPath("$.product.id").isNotEmpty()
				.jsonPath("$.product.name").isEqualTo("Mesa comedor")
				.jsonPath("$.product.category.name").isEqualTo("Muebles");
	}

	@Test
	public void crear2Test() {

		Category category = service.findCategoryByName("Muebles").block();

		Product product = new Product("Mesa comedor", 100.00, category);

		client.post().uri(url)
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.accept(MediaType.APPLICATION_JSON_UTF8)
				.body(Mono.just(product), Product.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
				.expectBody(new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {
				})
				// .expectBody(Product.class)
				.consumeWith(response -> {
					Object o = response.getResponseBody().get("product");
					Product p = new ObjectMapper().convertValue(o, Product.class);
					// Product p = response.getResponseBody();
					Assertions.assertThat(p.getId()).isNotEmpty();
					Assertions.assertThat(p.getName()).isEqualTo("Mesa comedor");
					Assertions.assertThat(p.getCategory().getName()).isEqualTo("Muebles");
				});
	}

	@Test
	public void editarTest() {

		Product product = service.findByName("Sony Notebook").block();
		Category category = service.findCategoryByName("Electrónico").block();

		Product productEdited = new Product("Asus Notebook", 700.00, category);

		client.put().uri(url + "/{id}", Collections.singletonMap("id",
				product.getId()))
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.accept(MediaType.APPLICATION_JSON_UTF8)
				.body(Mono.just(productEdited), Product.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.name").isEqualTo("Asus Notebook")
				.jsonPath("$.category.name").isEqualTo("Electrónico");

	}

	@Test
	public void eliminarTest() {
		Product product = service.findByName("Mica Cómoda 5 Cajones").block();
		client.delete()
				.uri(url + "/{id}", Collections.singletonMap("id", product.getId()))
				.exchange()
				.expectStatus().isNoContent()
				.expectBody()
				.isEmpty();

		client.get()
				.uri(url + "/{id}", Collections.singletonMap("id", product.getId()))
				.exchange()
				.expectStatus().isNotFound()
				.expectBody()
				.isEmpty();
	}

}
