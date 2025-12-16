package com.example.ogani.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("OGANI REST API DOCUMENT")
                        .contact(new Contact().name("Nguyễn Quang Quyền").email("quyenquy053@gmail.com").url("https://www.facebook.com/quyen.quy.35380/"))
                        .termsOfService("http://swagger.io/terms/")
                        .license(new License().name("Apache 2.0")
                                .url("http://springdoc.org")));    }
}
