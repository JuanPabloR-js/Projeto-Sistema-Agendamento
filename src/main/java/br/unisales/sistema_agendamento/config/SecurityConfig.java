package br.unisales.sistema_agendamento.config;

import br.unisales.sistema_agendamento.auth.service.CustomUserDetailsService;
import br.unisales.sistema_agendamento.security.JwtAuthenticationFilter;
import br.unisales.sistema_agendamento.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @apiNote Classe responsável pelas regras de segurança da aplicação.
 * @author Juan Pablo Rocha Hempel
 * @since 02.09.2026
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtService jwtService,
            CustomUserDetailsService userDetailsService
    ) throws Exception {

        // Criado somente para a cadeia do Spring Security.
        // Não declare outro @Bean para este filtro.
        JwtAuthenticationFilter jwtFilter =
                new JwtAuthenticationFilter(jwtService, userDetailsService);

        return http
                // Usamos token no cabeçalho, sem autenticação por cookies.
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                        )
                )

                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Cadastro público cria apenas contas CLIENTE.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/auth/login",
                                "/auth/cadastro"
                        ).permitAll()

                        // Inclui a consulta pública de horários disponíveis.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/barbeiros",
                                "/barbeiros/**",
                                "/servicos",
                                "/servicos/**"
                        ).permitAll()

                        .requestMatchers(
                                "/barbeiros",
                                "/barbeiros/**",
                                "/servicos",
                                "/servicos/**"
                        ).hasRole("ADMIN")

                        /*
                         * As rotas atuais de Cliente aceitam IDs arbitrários.
                         * Ficam administrativas; o usuário consulta e altera
                         * seus dados básicos em /usuarios/me.
                         */
                        .requestMatchers(
                                "/clientes",
                                "/clientes/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/agendamentos"
                        ).hasRole("CLIENTE")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/agendamentos"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/agendamentos/meus"
                        ).hasAnyRole("CLIENTE", "BARBEIRO")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/agendamentos/*"
                        ).hasAnyRole("CLIENTE", "BARBEIRO", "ADMIN")

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

                        .requestMatchers(
                                "/agendamentos",
                                "/agendamentos/**"
                        ).denyAll()

                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }
}