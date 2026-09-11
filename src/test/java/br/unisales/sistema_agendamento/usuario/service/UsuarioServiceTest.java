package br.unisales.sistema_agendamento.usuario.service;

import br.unisales.sistema_agendamento.exception.ResourceNotFoundException;
import br.unisales.sistema_agendamento.usuario.dto.TrocarSenhaRequestDTO;
import br.unisales.sistema_agendamento.usuario.model.Role;
import br.unisales.sistema_agendamento.usuario.model.Usuario;
import br.unisales.sistema_agendamento.usuario.repository.UsuarioRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Testes unitários do UsuarioService.
 *
 * Repository e PasswordEncoder são simulados com Mockito,
 * então os testes não dependem de banco de dados real.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    // Simula o acesso ao banco.
    @Mock
    private UsuarioRepository usuarioRepository;

    // Simula a comparação e criptografia de senhas.
    @Mock
    private PasswordEncoder passwordEncoder;

    private UsuarioService usuarioService;

    // Cria um novo service antes de cada teste usando os mocks.
    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(
                usuarioRepository,
                passwordEncoder
        );
    }

    @Test
    void deveBuscarUsuarioPorId() {

        Usuario usuario = new Usuario(
                "Breno",
                "breno@email.com",
                "27999999999",
                "senha",
                Role.CLIENTE
        );

        // Define o comportamento esperado do Repository para o ID informado.
        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        Usuario resultado =
                usuarioService.buscarEntidadePorId(1L);

        // Confirma que o Service retornou o usuário encontrado.
        assertEquals(usuario, resultado);
    }

    @Test
    void deveLancarErroQuandoUsuarioPorIdNaoExistir() {

        // Simula uma busca sem resultado.
        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        // O Service deve transformar a ausência em uma exceção de negócio.
        assertThrows(
                ResourceNotFoundException.class,
                () -> usuarioService.buscarEntidadePorId(99L)
        );
    }

    @Test
    void deveBuscarUsuarioPorEmail() {

        Usuario usuario = new Usuario(
                "Breno",
                "breno@email.com",
                "27999999999",
                "senha",
                Role.CLIENTE
        );

        when(usuarioRepository.findByEmail("breno@email.com"))
                .thenReturn(Optional.of(usuario));

        Usuario resultado =
                usuarioService.buscarEntidadePorEmail("breno@email.com");

        assertEquals(usuario, resultado);
    }

    @Test
    void deveTrocarSenhaQuandoSenhaAtualEstiverCorreta() {

        Usuario usuario = new Usuario(
                "Breno",
                "breno@email.com",
                "27999999999",
                "senhaCriptografadaAntiga",
                Role.CLIENTE
        );

        TrocarSenhaRequestDTO dto =
                new TrocarSenhaRequestDTO(
                        "senhaAtual",
                        "novaSenha"
                );

        // Simula um usuário autenticado pelo email.
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(
                        "breno@email.com",
                        null
                )
        );

        when(usuarioRepository.findByEmail("breno@email.com"))
                .thenReturn(Optional.of(usuario));

        // Simula a validação correta da senha atual.
        when(passwordEncoder.matches(
                "senhaAtual",
                "senhaCriptografadaAntiga"
        )).thenReturn(true);

        // Simula o hash gerado para a nova senha.
        when(passwordEncoder.encode("novaSenha"))
                .thenReturn("novaSenhaCriptografada");

        usuarioService.trocarSenha(dto);

        // Confirma que a senha armazenada foi substituída pelo novo hash.
        assertEquals(
                "novaSenhaCriptografada",
                usuario.getSenha()
        );

        // Confirma que a alteração foi enviada para persistência.
        verify(usuarioRepository).save(usuario);
    }

    // Evita que a autenticação simulada de um teste afete os próximos.
    @AfterEach
    void limparSecurityContext() {
        SecurityContextHolder.clearContext();
    }

}