package br.unisales.sistema_agendamento.config;

import br.unisales.sistema_agendamento.security.JwtAuthenticationFilter;
import br.unisales.sistema_agendamento.auth.service.CustomUserDetailsService;
import br.unisales.sistema_agendamento.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
 * @apiNote Classe responsável pela configuração de segurança da aplicação.
 * @author Juan Pablo Rocha Hempel
 * @since 02.09.2026
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    /*
     * Define o BCrypt como algoritmo de criptografia das senhas.
     * O BCrypt também é utilizado pelo Spring Security para comparar
     * a senha do login com a senha criptografada do banco.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
     * Disponibiliza o gerenciador que realiza a autenticação
     * utilizando o UserDetailsService e o PasswordEncoder.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /*
     * Cria manualmente o filtro JWT.
     * Por isso JwtAuthenticationFilter não deve possuir @Component.
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService
    ) {
        return new JwtAuthenticationFilter(
                jwtService,
                userDetailsService
        );
    }

    /*
     * Configura quais rotas são públicas, quais são protegidas
     * e como o JWT será processado.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtFilter
    ) throws Exception {

        return http

                /*
                 * CSRF é normalmente utilizado em aplicações que mantêm
                 * autenticação por sessão e cookies.
                 * Como nossa API utiliza JWT, ele será desabilitado.
                 */
                .csrf(csrf -> csrf.disable())

                // Não utilizaremos a tela de login padrão do Spring.
                .formLogin(form -> form.disable())

                // Não utilizaremos autenticação HTTP Basic.
                .httpBasic(basic -> basic.disable())

                /*
                 * Define a API como stateless.
                 * Isso significa que o servidor não guardará uma sessão.
                 * Cada requisição deve enviar seu próprio token.
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                /*
                 * Retorna 401 quando alguém tenta acessar uma rota
                 * protegida sem estar autenticado.
                 */
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(
                                new HttpStatusEntryPoint(
                                        HttpStatus.UNAUTHORIZED
                                )
                        )
                )

                /*
                 * Define as permissões dos endpoints.
                 */
                .authorizeHttpRequests(authorize -> authorize

                        /*
                         * Login e documentação são públicos.
                         * Não é necessário enviar token.
                         */
                        .requestMatchers(
                                "/auth/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        /*
                         * Consulta de barbeiros e serviços será pública.
                         * Posteriormente podemos separar por metodo HTTP.
                         */
                        .requestMatchers(
                                "/barbeiros/**",
                                "/servicos/**"
                        ).permitAll()

                        /*
                         * Qualquer endpoint não informado anteriormente
                         * exigirá autenticação.
                         */
                        .anyRequest().authenticated()
                )

                /*
                 * Coloca nosso filtro JWT antes do filtro padrão
                 * de autenticação por usuário e senha.
                 */
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .build();
    }
}