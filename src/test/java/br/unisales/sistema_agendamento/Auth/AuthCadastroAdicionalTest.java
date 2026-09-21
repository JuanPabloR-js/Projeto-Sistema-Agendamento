package br.unisales.sistema_agendamento.auth.service;

import br.unisales.sistema_agendamento.auth.dto.CadastroClienteRequestDTO;
import br.unisales.sistema_agendamento.cliente.dto.ClienteRequestDTO;
import br.unisales.sistema_agendamento.cliente.dto.ClienteResponseDTO;
import br.unisales.sistema_agendamento.cliente.service.ClienteService;
import br.unisales.sistema_agendamento.exception.ConflictException;
import br.unisales.sistema_agendamento.security.JwtService;
import br.unisales.sistema_agendamento.usuario.model.Role;
import br.unisales.sistema_agendamento.usuario.model.Usuario;
import br.unisales.sistema_agendamento.usuario.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @apiNote Testes do cadastro que integra a conta de usuário ao perfil de cliente.
 * @author Juan Pablo Rocha Hempel
 * @since 20.09.2026
 */
@ExtendWith(MockitoExtension.class)
class AuthCadastroAdicionalTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private JwtService jwtService;

    @Mock
    private ClienteService clienteService;

    @InjectMocks
    private AuthService authService;

    private CadastroClienteRequestDTO request;
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        request = new CadastroClienteRequestDTO(
                "Juan",
                "juan@teste.com",
                "Senha123",
                "27999999999"
        );

        usuario = new Usuario(
                request.nome(),
                request.email(),
                request.telefone(),
                "hash-simulado",
                Role.CLIENTE
        );

        usuario.setId(1L);
    }

    @Test
    void deveCriarUsuarioComRoleClienteEVincularPerfil() {
        prepararCriacaoDoUsuario();

        ClienteResponseDTO respostaEsperada = ClienteResponseDTO.builder()
                .id(10L)
                .usuarioId(1L)
                .nome("Juan")
                .telefone("27999999999")
                .build();

        when(clienteService.criar(any(ClienteRequestDTO.class)))
                .thenReturn(respostaEsperada);

        ClienteResponseDTO resposta = authService.cadastrar(request);

        // Confere que o backend definiu CLIENTE e enviou a senha ao UsuarioService.
        verify(usuarioService).criar(
                "Juan",
                "juan@teste.com",
                "27999999999",
                "Senha123",
                Role.CLIENTE
        );

        // Captura o DTO usado para criar o perfil do cliente.
        ArgumentCaptor<ClienteRequestDTO> captor =
                ArgumentCaptor.forClass(ClienteRequestDTO.class);

        verify(clienteService).criar(captor.capture());

        assertAll(
                () -> assertEquals(1L, captor.getValue().getUsuarioId()),
                () -> assertEquals(
                        "27999999999",
                        captor.getValue().getTelefone()
                ),
                () -> assertSame(respostaEsperada, resposta)
        );

        // O cadastro atual devolve o perfil; o token é obtido no login.
        verifyNoInteractions(authenticationManager, jwtService);
    }

    @Test
    void naoDeveCriarPerfilQuandoEmailJaExistir() {
        when(usuarioService.criar(
                request.nome(),
                request.email(),
                request.telefone(),
                request.senha(),
                Role.CLIENTE
        )).thenThrow(new ConflictException("E-mail já cadastrado."));

        assertThrows(
                ConflictException.class,
                () -> authService.cadastrar(request)
        );

        verifyNoInteractions(clienteService);
    }

    @Test
    void devePropagarFalhaNaCriacaoDoPerfil() {
        prepararCriacaoDoUsuario();

        ConflictException falha =
                new ConflictException("Perfil já cadastrado.");

        when(clienteService.criar(any(ClienteRequestDTO.class)))
                .thenThrow(falha);

        ConflictException recebida = assertThrows(
                ConflictException.class,
                () -> authService.cadastrar(request)
        );

        assertSame(falha, recebida);
        verifyNoInteractions(jwtService);
    }

    private void prepararCriacaoDoUsuario() {
        when(usuarioService.criar(
                request.nome(),
                request.email(),
                request.telefone(),
                request.senha(),
                Role.CLIENTE
        )).thenReturn(usuario);
    }
}