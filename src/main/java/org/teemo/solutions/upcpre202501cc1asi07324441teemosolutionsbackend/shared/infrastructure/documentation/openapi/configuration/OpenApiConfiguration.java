package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.shared.infrastructure.documentation.openapi.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";
    private static final String SECURITY_SCHEME = "bearer";
    private static final String SECURITY_FORMAT = "JWT";

    // En OpenApiConfiguration.java
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Teemo Solutions API")
                        .version("1.0.0")
                        .description("Documentación de la API de optimización de rutas marítimas")
                )
                .externalDocs(new ExternalDocumentation()
                        .description("Documentación técnica")
                        .url("https://teemo-solutions.com/docs"))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

    private Info apiInfo() {
        return new Info()
                .title("Teemo Solutions Mapping API")
                .description("API documentation for Teemo Solutions Maritime Route Optimization System")
                .version("1.0.0")
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0"));
    }

    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme(SECURITY_SCHEME)
                                .bearerFormat(SECURITY_FORMAT));
    }

    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement()
                .addList(SECURITY_SCHEME_NAME);
    }
}