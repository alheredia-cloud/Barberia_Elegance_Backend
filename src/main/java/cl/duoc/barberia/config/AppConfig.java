package cl.duoc.barberia.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class AppConfig {

    /** Reloj inyectable (zona horaria de Chile); permite testear reglas de fechas. */
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("America/Santiago"));
    }

    /** Swagger: agrega el boton "Authorize" para probar endpoints protegidos con el JWT. */
    @Bean
    public OpenAPI openAPI() {
        final String scheme = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Barberia Elegance API")
                        .version("v1")
                        .description("API REST de la tienda y agenda de Barberia Elegance (DSY1104 - Evaluacion 3). "
                                + "Para endpoints protegidos: ejecutar POST /api/v1/auth/login, copiar el accessToken "
                                + "y pegarlo en el boton Authorize.")
                        .contact(new Contact().name("Equipo Barberia Elegance")))
                .addSecurityItem(new SecurityRequirement().addList(scheme))
                .components(new Components().addSecuritySchemes(scheme, new SecurityScheme()
                        .name(scheme)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
