package br.unisales.sistema_agendamento.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * @apiNote Classe de configuração do Swagger.
 * @author Juan Pablo Rocha Hempel
 * @since 02.09.2026
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Barbearia API",
                version = "1.0",
                description = """
                        API REST para gerenciamento de usuários, clientes,
                        barbeiros, serviços e agendamentos de uma barbearia.
                        """,
                contact = @Contact(
                        name = "Juan Pablo - Breno Clemente - Moacyr Pereira - João Pereira - Mateus Alves",
                        email = "contato@barbearia.com"
                ),
                license = @License(
                        name = "Projeto acadêmico"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080",
                        description = "Servidor local"
                )
        }
)
//Cria botão de Authorize no Swagger
@SecurityScheme(
        name = OpenApiConfig.JWT_SECURITY_SCHEME,
        description = "Informe o token JWT recebido no endpoint de login.",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenApiConfig {

    public static final String JWT_SECURITY_SCHEME = "bearerAuth";

}