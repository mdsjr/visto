package com.visto.visto.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Visto! API",
                version = "0.1.0",
                description = "Helpdesk pensado pelo técnico, para o técnico — sem perder o usuário de vista.",
                contact = @Contact(name = "Moacir Domingos", url = "https://github.com/mdsjr"),
                license = @License(name = "MIT")
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Informe o token retornado por /auth/login ou /auth/register"
)
public class OpenApiConfig {
}
