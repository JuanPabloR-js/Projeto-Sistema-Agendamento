// Feito por: moacyr dev2

package br.unisales.sistema_agendamento.cliente.service;

import br.unisales.sistema_agendamento.cliente.domain.Cliente;
import br.unisales.sistema_agendamento.cliente.dto.ClienteRequestDTO;
import br.unisales.sistema_agendamento.cliente.dto.ClienteResponseDTO;
import br.unisales.sistema_agendamento.cliente.repository.ClienteRepository;
import br.unisales.sistema_agendamento.exception.ConflictException;
import br.unisales.sistema_agendamento.exception.ResourceNotFoundException;
import br.unisales.sistema_agendamento.usuario.model.Role;
import br.unisales.sistema_agendamento.usuario.model.Usuario;
import br.unisales.sistema_agendamento.usuario.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private Usuario usuario;

    private ClienteService clienteService;

    @BeforeEach
    void configurar() {
        clienteService = new ClienteService(clienteRepository, usuarioService);
    }

    @Test
    @DisplayName("Deve cadastrar cliente com sucesso")
    void deveCadastrarClienteComSucesso() {
        Long usuarioId = 1L;
        ClienteRequestDTO dto = new ClienteRequestDTO(
                usuarioId,
                "27999999999"
        );

        when(clienteRepository.existsByUsuarioId(usuarioId)).thenReturn(false);
        when(usuarioService.buscarEntidadePorId(usuarioId)).thenReturn(usuario);
        when(usuario.getId()).thenReturn(usuarioId);
        when(usuario.getNome()).thenReturn("João Silva");
        when(usuario.getRole()).thenReturn(Role.CLIENTE);
        when(clienteRepository.save(any(Cliente.class)))
                .thenAnswer(invocation -> {
                    Cliente cliente = invocation.getArgument(0);
                    cliente.setId(10L);
                    return cliente;
                });

        ClienteResponseDTO resposta = clienteService.criar(dto);

        assertEquals(10L, resposta.getId());
        assertEquals(usuarioId, resposta.getUsuarioId());
        assertEquals("João Silva", resposta.getNome());
        assertEquals("27999999999", resposta.getTelefone());

        ArgumentCaptor<Cliente> captor =
                ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(captor.capture());

        assertSame(usuario, captor.getValue().getUsuario());
        assertEquals("27999999999", captor.getValue().getTelefone());
    }

    @Test
    @DisplayName("Não deve cadastrar cliente quando usuário já possui perfil")
    void naoDeveCadastrarClienteParaUsuarioDuplicado() {
        Long usuarioId = 1L;
        ClienteRequestDTO dto = new ClienteRequestDTO(
                usuarioId,
                "27999999999"
        );

        when(clienteRepository.existsByUsuarioId(usuarioId)).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> clienteService.criar(dto)
        );

        assertTrue(exception.getMessage().contains(
                "já possui um perfil de cliente"
        ));
        verifyNoInteractions(usuarioService);
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Não deve cadastrar cliente para usuário inexistente")
    void naoDeveCadastrarClienteParaUsuarioInexistente() {
        Long usuarioId = 999L;
        ClienteRequestDTO dto = new ClienteRequestDTO(
                usuarioId,
                "27999999999"
        );

        when(clienteRepository.existsByUsuarioId(usuarioId)).thenReturn(false);
        when(usuarioService.buscarEntidadePorId(usuarioId))
                .thenThrow(new ResourceNotFoundException(
                        "Usuário não encontrado."
                ));

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> clienteService.criar(dto)
        );

        assertTrue(exception.getMessage().contains("Usuário não encontrado"));
        verify(clienteRepository, never()).save(any(Cliente.class));
    }
}
