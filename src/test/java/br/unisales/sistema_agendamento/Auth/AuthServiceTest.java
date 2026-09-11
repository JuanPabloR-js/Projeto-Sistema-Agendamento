package br.unisales.sistema_agendamento.Auth;

import br.unisales.sistema_agendamento.auth.dto.LoginRequestDTO;
import br.unisales.sistema_agendamento.auth.dto.LoginResponseDTO;
import br.unisales.sistema_agendamento.auth.service.AuthService;
import br.unisales.sistema_agendamento.auth.service.CustomUserDetailsService;
import br.unisales.sistema_agendamento.security.JwtService;
import br.unisales.sistema_agendamento.usuario.model.Role;
import br.unisales.sistema_agendamento.usuario.model.Usuario;
import br.unisales.sistema_agendamento.usuario.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * @apiNote Classe de testes unitários do serviço de autenticação.
 * @author Juan Pablo Rocha Hempel
 * @since 11.09.2026
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // Cria versões simuladas das dependências do AuthService.
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private JwtService jwtService;

    // Cria o AuthService e injeta os mocks declarados acima.
    @InjectMocks
    private AuthService authService;

    private LoginRequestDTO request;
    private Usuario usuario;
    private UserDetails userDetails;

    // Executa antes de cada teste, preparando dados novos.
    @BeforeEach
    void configurar() {
        request = new LoginRequestDTO(
                "juan@example.com",
                "Senha123"
        );

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Juan Pablo");
        usuario.setEmail("juan@example.com");
        usuario.setSenha("hash-simulado");
        usuario.setRole(Role.CLIENTE);

        /*
         * Aqui não precisamos gerar um BCrypt real.
         * A autenticação será simulada pelo AuthenticationManager.
         */
        userDetails = User.builder()
                .username(usuario.getEmail())
                .password(usuario.getSenha())
                .roles(usuario.getRole().name())
                .build();
    }

    @Test
    void deveRetornarTokenEDadosDoUsuarioQuandoLoginForValido() {
        // PREPARAR: define como as dependências devem responder.
        var autenticacao = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenReturn(autenticacao);

        when(userDetailsService.loadUserByUsername(request.email()))
                .thenReturn(userDetails);

        when(usuarioService.buscarEntidadePorEmail(request.email()))
                .thenReturn(usuario);

        when(jwtService.gerarToken(userDetails))
                .thenReturn("token-de-teste");

        // EXECUTAR: chama o metodo real que queremos testar.
        LoginResponseDTO response = authService.login(request);

        // VERIFICAR: confere os dados devolvidos pelo login.
        assertAll(
                () -> assertEquals("token-de-teste", response.token()),
                () -> assertEquals("Bearer", response.tipo()),
                () -> assertEquals(1L, response.usuarioId()),
                () -> assertEquals("Juan Pablo", response.nome()),
                () -> assertEquals("juan@example.com", response.email()),
                () -> assertEquals("CLIENTE", response.role())
        );

        // Confirma que o e-mail e a senha foram enviados para autenticação.
        verify(authenticationManager).authenticate(argThat(
                dados -> request.email().equals(dados.getPrincipal())
                        && request.senha().equals(dados.getCredentials())
        ));

        // Confirma que o serviço pediu a geração do JWT.
        verify(jwtService).gerarToken(userDetails);
    }

    @Test
    void deveLancarExcecaoENaoGerarTokenQuandoSenhaForIncorreta() {
        // Simula o Spring Security rejeitando as credenciais.
        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenThrow(new BadCredentialsException("Credenciais inválidas"));

        // Confirma que a exceção é propagada pelo AuthService.
        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request)
        );

        /*
         * Após a falha, o AuthService deve interromper o fluxo.
         * Não pode buscar os dados da resposta nem gerar um token.
         */
        verifyNoInteractions(
                userDetailsService,
                usuarioService,
                jwtService
        );
    }

    @Test
    void deveLancarExcecaoENaoGerarTokenQuandoUsuarioEstiverDesativado() {
        // Simula o Spring Security bloqueando um usuário desativado.
        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenThrow(new DisabledException("Usuário desativado"));

        assertThrows(
                DisabledException.class,
                () -> authService.login(request)
        );

        verifyNoInteractions(
                userDetailsService,
                usuarioService,
                jwtService
        );
    }
}
