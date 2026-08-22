package br.com.fiap.solidari.backend.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Solidari API",
                version = "1.0",
                description = "API REST do app Solidari (Fase 2 - Spring Boot): usuários, "
                        + "parceiros de cashback e doações de crédito com cálculo de impacto."
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = io.swagger.v3.oas.annotations.enums.SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = "Token JWT obtido em /api/auth/login ou /api/auth/registrar"
)
public class OpenApiConfig {
}
