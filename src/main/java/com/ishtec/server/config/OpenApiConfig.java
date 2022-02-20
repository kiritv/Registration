package com.ishtec.server.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openApiConfiguration() {
        return new OpenAPI()
                .components(new Components()
                		.addSecuritySchemes("http-bearer", new SecurityScheme()
                				.type(SecurityScheme.Type.HTTP)
                				.scheme("bearer")
                				.bearerFormat("JWT")
                				.description("Please use /users/login REST call to retrieve JWT")))
                .security(Arrays.asList(new SecurityRequirement().addList("http-bearer")))
                .info(new Info()
                		.title("TTB API Docs")
                        .description("TTB API Description. All requests need to be authorized.")
                        .version("1.0.0"));
    }
}
