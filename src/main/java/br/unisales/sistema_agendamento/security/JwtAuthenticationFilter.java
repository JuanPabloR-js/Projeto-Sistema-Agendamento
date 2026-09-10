package br.unisales.sistema_agendamento.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @apiNote Filtro responsável por autenticar requisições utilizando JWT.
 * @author Juan Pablo Rocha Hempel
 * @since 02.09.2026
 */

//AINDA FALTA COLOCAR O CONSTUMDETAILS DO USUARIO PARA FUNCIONAR
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService
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

        String cabecalhoAutorizacao =
                request.getHeader("Authorization");

        if (cabecalhoAutorizacao == null
                || !cabecalhoAutorizacao.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = cabecalhoAutorizacao.substring(7);

        try {
            autenticarUsuario(token, request);
        } catch (
                JwtException
                | AuthenticationException
                | IllegalArgumentException exception
        ) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void autenticarUsuario(
            String token,
            HttpServletRequest request
    ) {
        String email = jwtService.extrairEmail(token);

        boolean usuarioNaoAutenticado =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication() == null;

        if (email == null || !usuarioNaoAutenticado) {
            return;
        }

        UserDetails usuario =
                userDetailsService.loadUserByUsername(email);

        if (!jwtService.tokenValido(token, usuario)) {
            return;
        }

        UsernamePasswordAuthenticationToken autenticacao =
                new UsernamePasswordAuthenticationToken(
                        usuario,
                        null,
                        usuario.getAuthorities()
                );

        autenticacao.setDetails(
                new WebAuthenticationDetailsSource()
                        .buildDetails(request)
        );

        SecurityContextHolder
                .getContext()
                .setAuthentication(autenticacao);
    }
}