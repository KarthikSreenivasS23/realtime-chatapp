package com.chatspot.chatapp.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "ChatSpot API",
        version = "1.0",
        description = "API documentation for the ChatSpot backend application."
    )
)

@SecurityScheme(
        name = "bearerAuth", // The nickname for this security rule
        description = "JWT Bearer Token", // A friendly description for the UI
        scheme = "bearer", // The type of scheme (e.g., Bearer, Basic)
        type = SecuritySchemeType.HTTP, // We're using standard HTTP authentication
        bearerFormat = "JWT", // A hint that the token is a JWT
        in = SecuritySchemeIn.HEADER // Where to put the token (in the request header)
)

public class OpenApiConfig {
}
