package br.unisales.sistema_agendamento.config;

import br.unisales.sistema_agendamento.security.JwtAuthenticationFilter;
import br.unisales.sistema_agendamento.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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

                        // Consultas públicas
                        .requestMatchers(
                                HttpMethod.GET,
                                "/barbeiros",
                                "/barbeiros/**",
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

                        // Agendamentos
                        .requestMatchers(
                                HttpMethod.POST,
                                "/agendamentos"
                        ).hasRole("CLIENTE")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/agendamentos/meus"
                        ).hasAnyRole("CLIENTE", "BARBEIRO")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/agendamentos"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/agendamentos/*/cancelar"
                        ).hasAnyRole("CLIENTE", "ADMIN")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/agendamentos/*/confirmar"
                        ).hasRole("BARBEIRO")
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/agendamentos/*/concluir"
                        ).hasAnyRole("BARBEIRO", "ADMIN")

                        // Qualquer outra rota exige autenticação
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    //Esse objeto será usado para criptografar senhas.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}