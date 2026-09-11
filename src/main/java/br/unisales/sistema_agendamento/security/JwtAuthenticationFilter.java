package br.unisales.sistema_agendamento.security;

import br.unisales.sistema_agendamento.auth.service.CustomUserDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @apiNote Filtro responsável por autenticar requisições que possuem token JWT.
 * @author Juan Pablo Rocha Hempel
 * @since 11.09.2026
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    /*
     * Este metodo será executado uma vez em cada requisição.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        /*
         * Procura o cabeçalho:
         * Authorization: Bearer token-aqui
         */
        String authorization =
                request.getHeader(HttpHeaders.AUTHORIZATION);

        /*
         * Se não houver token, o filtro apenas continua.
         *
         * Depois, o SecurityConfig decidirá se aquele endpoint
         * permite acesso sem autenticação.
         */
        if (authorization == null
                || !authorization.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        /*
         * Remove a palavra "Bearer " e mantém apenas o JWT.
         * O número 7 representa os sete caracteres de "Bearer ".
         */
        String token = authorization.substring(7);

        try {
            // Descobre qual e-mail está armazenado dentro do token.
            String email = jwtService.extrairEmail(token);

            /*
             * Só realiza a autenticação quando:
             * 1. O token possui um e-mail;
             * 2. Ainda não existe autenticação nesta requisição.
             */
            if (email != null
                    && SecurityContextHolder.getContext()
                    .getAuthentication() == null) {

                UserDetails usuario =
                        userDetailsService.loadUserByUsername(email);

                // Confere o dono, a assinatura e a expiração do token.
                if (jwtService.tokenValido(token, usuario)) {

                    /*
                     * Cria a autenticação reconhecida pelo Spring.
                     *
                     * O segundo valor é null porque a senha não precisa
                     * ser enviada novamente: o JWT já foi validado.
                     */
                    UsernamePasswordAuthenticationToken autenticacao =
                            new UsernamePasswordAuthenticationToken(
                                    usuario,
                                    null,
                                    usuario.getAuthorities()
                            );

                    /*
                     * Adiciona detalhes da requisição, como endereço IP
                     * e informações relacionadas à conexão.
                     */
                    autenticacao.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    /*
                     * Salva a autenticação no contexto da requisição.
                     *
                     * A partir daqui, os controllers e services conseguem
                     * identificar qual usuário está autenticado.
                     */
                    SecurityContextHolder.getContext()
                            .setAuthentication(autenticacao);
                }
            }

        } catch (JwtException | IllegalArgumentException exception) {
            /*
             * Caso o token esteja inválido, alterado ou expirado,
             * removemos qualquer autenticação existente.
             *
             * O SecurityConfig impedirá o acesso se a rota for protegida.
             */
            SecurityContextHolder.clearContext();
        }

        // Continua a requisição para o próximo filtro ou controller.
        filterChain.doFilter(request, response);
    }
}