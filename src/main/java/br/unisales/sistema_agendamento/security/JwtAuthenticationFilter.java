package br.unisales.sistema_agendamento.security;

import br.unisales.sistema_agendamento.auth.service.CustomUserDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @apiNote Filtro responsável por validar o JWT e autenticar a requisição.
 * @author Juan Pablo Rocha Hempel
 * @since 02.09.2026
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    private final AccountStatusUserDetailsChecker verificadorDeConta =
            new AccountStatusUserDetailsChecker();

    public JwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorization =
                request.getHeader(HttpHeaders.AUTHORIZATION);

        // Sem token: as regras do SecurityConfig decidirão o acesso.
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Não substitui uma autenticação já estabelecida.
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);

        try {
            // O JwtService verifica assinatura e expiração ao ler o token.
            String email = jwtService.extrairEmail(token);

            if (email == null || email.isBlank()) {
                throw new BadCredentialsException("Token sem identificação");
            }

            UserDetails usuario =
                    userDetailsService.loadUserByUsername(email);

            // Verifica conta desativada, bloqueada ou expirada.
            verificadorDeConta.check(usuario);

            if (!jwtService.tokenValido(token, usuario)) {
                throw new BadCredentialsException("Token inválido");
            }

            var autenticacao = new UsernamePasswordAuthenticationToken(
                    usuario,
                    null,
                    usuario.getAuthorities()
            );

            autenticacao.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            // Registra o usuário autenticado apenas no contexto da requisição.
            var contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(autenticacao);
            SecurityContextHolder.setContext(contexto);

        } catch (JwtException | AuthenticationException |
                 IllegalArgumentException exception) {

            SecurityContextHolder.clearContext();

            // Se um Bearer foi enviado, mas não é válido, retorna 401.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Fora do try: erros do controller não são tratados como erros de JWT.
        filterChain.doFilter(request, response);
    }
}