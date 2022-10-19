package dev.team.springwebfluxapirest;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import dev.team.springwebfluxapirest.handler.ProductHandler;

@Configuration
public class RouterFunctionConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(ProductHandler handler) {

        return route(GET("/api/v2/products").or(GET("/api/v3/products")), handler::listar)
                .andRoute(GET("/api/v2/products/{id}"), handler::ver)
                .andRoute(POST("/api/v2/products"), handler::crear)
                .andRoute(PUT("/api/v2/products/{id}"), handler::editar)
                .andRoute(DELETE("/api/v2/products/{id}"), handler::eliminar)
                .andRoute(POST("/api/v2/products/upload/{id}"), handler::upload)
                .andRoute(POST("/api/v2/products/crear"), handler::crearConFoto);
    }

}
