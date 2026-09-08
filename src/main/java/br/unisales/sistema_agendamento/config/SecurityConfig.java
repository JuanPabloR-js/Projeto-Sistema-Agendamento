package br.unisales.sistema_agendamento.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @apiNote Classe responsável pela configuração de segurança da API.
 * @author Juan Pablo Rocha Hempel
 * @since 02.09.2026
 */
@Configuration
@EnableMethodSecurity //Permite controlar as permissões
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            //Define as regras de segurança das requisições.
            //É como uma sequência de verificações pela qual cada requisição passa antes de chegar ao Controller.
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .formLogin(AbstractHttpConfigurer::disable)//Impede que o Spring abra aquela página automática de login no navegador, O login será feito pelo seu endpoint.

                .httpBasic(AbstractHttpConfigurer::disable)

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(autorizacao -> autorizacao

                        // Swagger
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Cadastro e login
                        .requestMatchers(
                                "/auth/cadastro",
                                "/auth/login"
                        ).permitAll()

                        // Consultas públicas de barbeiros
                        .requestMatchers(
                                HttpMethod.GET,
                                "/barbeiros",
                                "/barbeiros/**"
                        ).permitAll()

                        // Consultas públicas de serviços
                        .requestMatchers(
                                HttpMethod.GET,
                                "/servicos",
                                "/servicos/**"
                        ).permitAll()

                        // Administração dos barbeiros
                        .requestMatchers(
                                HttpMethod.POST,
                                "/barbeiros"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/barbeiros/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/barbeiros/**"
                        ).hasRole("ADMIN")

                        // Administração dos serviços
                        .requestMatchers(
                                HttpMethod.POST,
                                "/servicos"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/servicos/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/servicos/**"
                        ).hasRole("ADMIN")

                        // Cliente cria um agendamento
                        .requestMatchers(
                                HttpMethod.POST,
                                "/agendamentos"
                        ).hasRole("CLIENTE")

                        // Cliente e barbeiro visualizam seus agendamentos
                        .requestMatchers(
                                HttpMethod.GET,
                                "/agendamentos/meus"
                        ).hasAnyRole("CLIENTE", "BARBEIRO")

                        // Somente o administrador lista todos
                        .requestMatchers(
                                HttpMethod.GET,
                                "/agendamentos"
                        ).hasRole("ADMIN")

                        // Cliente ou administrador podem cancelar
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/agendamentos/*/cancelar"
                        ).hasAnyRole("CLIENTE", "ADMIN")

                        // Barbeiro confirma o agendamento
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/agendamentos/*/confirmar"
                        ).hasRole("BARBEIRO")

                        // Barbeiro ou administrador concluem
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/agendamentos/*/concluir"
                        ).hasAnyRole("BARBEIRO", "ADMIN")

                        // Qualquer outra rota exige login
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    //Esse objeto será usado para criptografar senhas.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //Esse objeto será utilizado posteriormente no AuthService para verificar email e senha:
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }
}