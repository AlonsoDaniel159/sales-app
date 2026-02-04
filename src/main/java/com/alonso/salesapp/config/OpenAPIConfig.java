package com.alonso.salesapp.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Sales App API",
                version = "1.0",
                description = "API documentation for the Sales App",
                contact = @Contact(
                        name = "Alonso Quispe",
                        email = "alonsodaniel619@gmail.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                ),
                termsOfService = "https://swagger.io/terms/"
        ),
        servers = @Server(
                url = "http://localhost:8080",
                description = "Dev"
        )
)
@Configuration
public class OpenAPIConfig {
}
