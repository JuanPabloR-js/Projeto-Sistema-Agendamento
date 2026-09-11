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

                        // Somente os endpoints públicos de autenticação.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/auth/login",
                                "/auth/register"
                        ).permitAll()

                        // Visitantes podem consultar profissionais e serviços.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/barbeiros",
                                "/barbeiros/**",
                                "/servicos",
                                "/servicos/**"
                        ).permitAll()

                        // Operações de alteração ficam restritas ao administrador.
                        .requestMatchers(
                                "/barbeiros",
                                "/barbeiros/**",
                                "/servicos",
                                "/servicos/**"
                        ).hasRole("ADMIN")

                        // As permissões específicas de agendamento
                        // serão acrescentadas quando implementarmos suas rotas.
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }
}