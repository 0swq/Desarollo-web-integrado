package com.autopartes.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_SEGURIDAD = "bearerAuth";

    @Bean
    public OpenAPI configuracionOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Autopartes")
                        .version("1.0.0")
                        .description("API REST para la gestion de un catalogo de autopartes: "
                                + "usuarios, productos, stock, carrito, ordenes y pagos.")
                        .contact(new Contact().name("Equipo Autopartes"))
                        .license(new License().name("MIT")))
                .components(new Components().addSecuritySchemes(ESQUEMA_SEGURIDAD,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Pega aqui el token que devuelve POST /api/auth/login. "
                                        + "El token ya incluye el prefijo 'Bearer '.")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_SEGURIDAD));
    }
}