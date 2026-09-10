package br.unisales.sistema_agendamento.security;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @apiNote Classe de testes do serviço responsável pelos tokens JWT.
 * @author Juan Pablo Rocha Hempel
 * @since 02.09.2026
 */
class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails usuario;

    @BeforeEach
    void prepararTeste() {
        String chaveSecreta =
                "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

        long tempoExpiracao = 3600000;

        jwtService = new JwtService(
                chaveSecreta,
                tempoExpiracao
        );

        usuario = User.builder()
                .username("cliente@email.com")
                .password("senha-criptografada")
                .authorities("ROLE_CLIENTE")
                .build();
    }

    @Test
    void deveGerarToken() { // confirma se o metodo realmente retorna um JWT
        String token = jwtService.gerarToken(usuario);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void deveExtrairEmailDoToken() { // gera um token e verifica se o email armazenado pode ser recuperado
        String token = jwtService.gerarToken(usuario);

        String email = jwtService.extrairEmail(token);

        assertEquals(
                "cliente@email.com",
                email
        );
    }

    @Test
    void deveConsiderarTokenValido() { // confirma se o token é válido
        String token = jwtService.gerarToken(usuario);

        boolean resultado =
                jwtService.tokenValido(token, usuario);

        assertTrue(resultado);
    }

    @Test
    void naoDeveAceitarTokenDeOutroUsuario() { // confirma que o token de cliente não é considerado válido para outro email
        String token = jwtService.gerarToken(usuario);

        UserDetails outroUsuario = User.builder()
                .username("outro@email.com")
                .password("outra-senha")
                .authorities("ROLE_CLIENTE")
                .build();

        boolean resultado =
                jwtService.tokenValido(token, outroUsuario);

        assertFalse(resultado);
    }
}
