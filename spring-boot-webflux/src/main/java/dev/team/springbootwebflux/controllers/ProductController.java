package dev.team.springbootwebflux.controllers;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.UUID;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;

import dev.team.springbootwebflux.models.documents.Category;
import dev.team.springbootwebflux.models.documents.Product;
import dev.team.springbootwebflux.models.services.ProductService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class ProductController {

    @Autowired
    private ProductService service;

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    @Value("${config.uploads.path}")
    private String path;

    @ModelAttribute("categories")
    public Flux<Category> categories() {
        return service.findAllCategoria();
    }

    @GetMapping("/uploads/{photo:.+}")
    public Mono<ResponseEntity<Resource>> verFoto(@PathVariable String photo)
            throws MalformedURLException {
        Path ruta = Paths.get(path).resolve(photo).toAbsolutePath();

        Resource imagen = new UrlResource(ruta.toUri());

        return Mono.just(
                ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + imagen.getFilename() + "\"")
                        .body(imagen));

    }

    @GetMapping("/ver/{id}")
    public Mono<String> ver(Model model, @PathVariable String id) {

        return service.findById(id)
                .doOnNext(p -> {
                    model.addAttribute("product", p);
                    model.addAttribute("title", "Detalle Producto");
                }).switchIfEmpty(Mono.just(new Product()))
                .flatMap(p -> {
                    if (p.getId() == null) {
                        return Mono.error(new InterruptedException("No extiste el producto"));
                    }
                    return Mono.just(p);
                }).then(Mono.just("ver"))
                .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto"));
    }

    @GetMapping({ "/listar", "/" })
    public Mono<String> listar(Model model) {
        System.out.println("Error=============================");

        Flux<Product> products = service.findAllWithNameUpperCase();

        products.subscribe(prod -> log.info(prod.getName()));

        model.addAttribute("products", products);
        model.addAttribute("title", "Listado de productos");
        return Mono.just("listar");
    }

    @GetMapping("/form")
    public Mono<String> crear(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("title", "Formulario de producto");
        model.addAttribute("button", "Crear");
        return Mono.just("form");
    }

    @PostMapping("/form")
    public Mono<String> guardar(@Valid Product producto, BindingResult result,
            Model model, @RequestPart FilePart file,
            SessionStatus status) {

        if (result.hasErrors()) {
            model.addAttribute("title", "Errores en formulario producto");
            model.addAttribute("button", "Guardar");
            return Mono.just("form");
        } else {
            status.setComplete();

            Mono<Category> categoria = service.findCategoriaById(producto.getCategory().getId());

            return categoria.flatMap(c -> {
                if (producto.getCreatedAt() == null) {
                    producto.setCreatedAt(new Date());
                }

                if (!file.filename().isEmpty()) {
                    producto.setPhoto(UUID.randomUUID().toString() + "-" + file.filename()
                            .replace(" ", "")
                            .replace(":", "")
                            .replace("\\", ""));
                }
                producto.setCategory(c);
                return service.save(producto);
            }).doOnNext(p -> {
                log.info(
                        "Categoria asignada: " + p.getCategory().getName() + " Id Cat: " +
                                p.getCategory().getId());
                log.info("Producto guardado: " + p.getName() + " Id: " + p.getId());
            })
                    .flatMap(p -> {
                        if (!file.filename().isEmpty()) {
                            return file.transferTo(new File(path + p.getPhoto()));
                        }
                        return Mono.empty();
                    })
                    .thenReturn("redirect:/listar?success=producto+guardado+con+exito");
        }
    }

    @GetMapping("/form/{id}")
    public Mono<String> editar(@PathVariable String id, Model model) {
        Mono<Product> productoMono = service.findById(id).doOnNext(p -> {
            log.info("Producto: " + p.getName());
        }).defaultIfEmpty(new Product());

        model.addAttribute("title", "Editar Producto");
        model.addAttribute("button", "Editar");
        model.addAttribute("product", productoMono);

        return Mono.just("form");
    }

    @GetMapping("/form-v2/{id}")
    public Mono<String> editarV2(@PathVariable String id, Model model) {

        return service.findById(id).doOnNext(p -> {
            log.info("Producto: " + p.getName());
            model.addAttribute("boton", "Editar");
            model.addAttribute("titulo", "Editar Producto");
            model.addAttribute("producto", p);
        }).defaultIfEmpty(new Product())
                .flatMap(p -> {
                    if (p.getId() == null) {
                        return Mono.error(new InterruptedException("No extiste el producto"));
                    }
                    return Mono.just(p);
                })
                .then(Mono.just("form"))
                .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto"));

    }

    @GetMapping("/eliminar/{id}")
    public Mono<String> eliminar(@PathVariable String id) {
        return service.findById(id)
                .defaultIfEmpty(new Product())
                .flatMap(p -> {
                    if (p.getId() == null) {
                        return Mono.error(new InterruptedException("No extiste el producto a eliminar!"));
                    }
                    return Mono.just(p);
                })
                .flatMap(p -> {
                    log.info("Eliminando producto: " + p.getName());
                    log.info("Eliminando producto Id: " + p.getId());
                    return service.delete(p);
                }).then(Mono.just("redirect:/listar?success=producto+eliminado+con+exito"))
                .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto+a+eliminar"));
    }

    @GetMapping("/listar-datadriver")
    public String listarDataDriver(Model model) {

        Flux<Product> productos = service.findAllWithNameUpperCase();

        productos.subscribe(prod -> log.info(prod.getName()));

        model.addAttribute("products", new ReactiveDataDriverContextVariable(productos, 1));
        model.addAttribute("title", "Listado de productos");
        return "listar";
    }

    @GetMapping("/listar-full")
    public String listarFull(Model model) {

        Flux<Product> productos = service.findAllWithNameUpperCaseRepeat();

        model.addAttribute("products", productos);
        model.addAttribute("title", "Listado de productos");
        return "listar";
    }

    @GetMapping("/listar-chunked")
    public String listarChunked(Model model) {

        Flux<Product> productos = service.findAllWithNameUpperCaseRepeat();

        model.addAttribute("products", productos);
        model.addAttribute("title", "Listado de productos");
        return "listar-chunked";
    }

}
