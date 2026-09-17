package br.unisales.sistema_agendamento.barbeiro.service;

import br.unisales.sistema_agendamento.barbeiro.dto.BarbeiroRequestDTO;
import br.unisales.sistema_agendamento.barbeiro.dto.BarbeiroResponseDTO;
import br.unisales.sistema_agendamento.barbeiro.model.Barbeiro;
import br.unisales.sistema_agendamento.barbeiro.repository.BarbeiroRepository;
import br.unisales.sistema_agendamento.exception.BusinessException;
import br.unisales.sistema_agendamento.exception.ConflictException;
import br.unisales.sistema_agendamento.exception.ResourceNotFoundException;
import br.unisales.sistema_agendamento.usuario.model.Role;
import br.unisales.sistema_agendamento.usuario.model.Usuario;
import br.unisales.sistema_agendamento.usuario.service.UsuarioService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitários do BarbeiroService.
 *
 * BarbeiroRepository e UsuarioService são simulados com Mockito.
 *
 * Isso significa que estes testes não dependem de:
 * - PostgreSQL;
 * - Flyway;
 * - banco de dados real;
 * - aplicação Spring rodando.
 */
@ExtendWith(MockitoExtension.class)
class BarbeiroServiceTest {

    // Simula o Repository responsável pelos barbeiros.
    @Mock
    private BarbeiroRepository barbeiroRepository;

    // Simula o UsuarioService.
    // O BarbeiroService utiliza o UsuarioService
    // para buscar o usuário associado ao barbeiro.
    @Mock
    private UsuarioService usuarioService;

    // Classe que realmente queremos testar.
    private BarbeiroService barbeiroService;


    /**
     * Este metodo é executado antes de cada teste.
     *
     * Criamos um BarbeiroService novo usando os mocks.
     */
    @BeforeEach
    void setUp() {

        barbeiroService = new BarbeiroService(
                barbeiroRepository,
                usuarioService
        );
    }


    /**
     * Testa a criação de um barbeiro válida.
     */
    @Test
    void deveCriarBarbeiro() {

        // Cria um usuário com a role correta.
        Usuario usuario = new Usuario(
                "Carlos",
                "carlos@email.com",
                "27999999999",
                "senha",
                Role.BARBEIRO
        );

        // DTO simulando os dados recebidos pela API.
        BarbeiroRequestDTO dto =
                new BarbeiroRequestDTO(
                        1L,
                        "Corte masculino"
                );

        // Simula que o UsuarioService encontrou o usuário.
        when(usuarioService.buscarEntidadePorId(1L))
                .thenReturn(usuario);

        // Simula que o usuário ainda NÃO possui perfil de barbeiro.
        when(barbeiroRepository.findByUsuarioId(usuario.getId()))
                .thenReturn(Optional.empty());

        /*
         * Quando o Service chamar save(),
         * devolvemos o próprio objeto recebido.
         */
        when(barbeiroRepository.save(
                org.mockito.ArgumentMatchers.any(Barbeiro.class)
        )).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        // Executa o metodo que estamos testando.
        BarbeiroResponseDTO resultado =
                barbeiroService.criar(dto);

        // Confirma a especialidade retornada.
        assertEquals(
                "Corte masculino",
                resultado.getEspecialidade()
        );

        // O barbeiro novo deve começar ativo.
        assertTrue(resultado.getAtivo());

        // Confirma que o Repository recebeu uma chamada de save().
        verify(barbeiroRepository)
                .save(org.mockito.ArgumentMatchers.any(Barbeiro.class));
    }


    /**
     * Testa a regra:
     *
     * somente usuários BARBEIRO podem possuir perfil de barbeiro.
     */
    @Test
    void deveLancarErroQuandoUsuarioNaoForBarbeiro() {

        Usuario usuario = new Usuario(
                "João",
                "joao@email.com",
                "27999999999",
                "senha",
                Role.CLIENTE
        );

        BarbeiroRequestDTO dto =
                new BarbeiroRequestDTO(
                        1L,
                        "Corte masculino"
                );

        when(usuarioService.buscarEntidadePorId(1L))
                .thenReturn(usuario);

        // Esperamos que a regra de negócio gere BusinessException.
        assertThrows(
                BusinessException.class,
                () -> barbeiroService.criar(dto)
        );
    }


    /**
     * Testa a tentativa de criar dois perfis
     * para o mesmo Usuario.
     */
    @Test
    void deveLancarErroQuandoUsuarioJaPossuirBarbeiro() {

        Usuario usuario = new Usuario(
                "Carlos",
                "carlos@email.com",
                "27999999999",
                "senha",
                Role.BARBEIRO
        );

        BarbeiroRequestDTO dto =
                new BarbeiroRequestDTO(
                        1L,
                        "Corte masculino"
                );

        Barbeiro barbeiroExistente =
                new Barbeiro(
                        usuario,
                        "Barba",
                        true
                );

        when(usuarioService.buscarEntidadePorId(1L))
                .thenReturn(usuario);

        // Simula que já existe um barbeiro para esse usuário.
        when(barbeiroRepository.findByUsuarioId(usuario.getId()))
                .thenReturn(Optional.of(barbeiroExistente));

        // O Service deve impedir a duplicação.
        assertThrows(
                ConflictException.class,
                () -> barbeiroService.criar(dto)
        );
    }


    /**
     * Testa a busca de um barbeiro existente.
     */
    @Test
    void deveBuscarBarbeiroPorId() {

        Usuario usuario = new Usuario(
                "Carlos",
                "carlos@email.com",
                "27999999999",
                "senha",
                Role.BARBEIRO
        );

        Barbeiro barbeiro =
                new Barbeiro(
                        usuario,
                        "Corte masculino",
                        true
                );

        when(barbeiroRepository.findById(1L))
                .thenReturn(Optional.of(barbeiro));

        BarbeiroResponseDTO resultado =
                barbeiroService.buscarPorId(1L);

        assertEquals(
                "Corte masculino",
                resultado.getEspecialidade()
        );

        assertTrue(resultado.getAtivo());
    }


    /**
     * Testa a busca por um barbeiro inexistente.
     */
    @Test
    void deveLancarErroQuandoBarbeiroNaoExistir() {

        when(barbeiroRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> barbeiroService.buscarPorId(99L)
        );
    }


    /**
     * Testa a listagem de todos os barbeiros.
     */
    @Test
    void deveListarTodosOsBarbeiros() {

        Usuario usuario1 = new Usuario(
                "Carlos",
                "carlos@email.com",
                "27999999999",
                "senha",
                Role.BARBEIRO
        );

        Usuario usuario2 = new Usuario(
                "Pedro",
                "pedro@email.com",
                "27988888888",
                "senha",
                Role.BARBEIRO
        );

        Barbeiro barbeiro1 =
                new Barbeiro(
                        usuario1,
                        "Corte masculino",
                        true
                );

        Barbeiro barbeiro2 =
                new Barbeiro(
                        usuario2,
                        "Barba",
                        true
                );

        when(barbeiroRepository.findAll())
                .thenReturn(List.of(
                        barbeiro1,
                        barbeiro2
                ));

        List<BarbeiroResponseDTO> resultado =
                barbeiroService.listarTodos();

        // Confirma que vieram dois barbeiros.
        assertEquals(
                2,
                resultado.size()
        );
    }


    /**
     * Testa a desativação de um barbeiro.
     */
    @Test
    void deveDesativarBarbeiro() {

        Usuario usuario = new Usuario(
                "Carlos",
                "carlos@email.com",
                "27999999999",
                "senha",
                Role.BARBEIRO
        );

        Barbeiro barbeiro =
                new Barbeiro(
                        usuario,
                        "Corte masculino",
                        true
                );

        when(barbeiroRepository.findById(1L))
                .thenReturn(Optional.of(barbeiro));

        when(barbeiroRepository.save(barbeiro))
                .thenReturn(barbeiro);

        BarbeiroResponseDTO resultado =
                barbeiroService.desativar(1L);

        // Depois de desativar, ativo deve ser false.
        assertFalse(resultado.getAtivo());

        verify(barbeiroRepository)
                .save(barbeiro);
    }


    /**
     * Testa a ativação de um barbeiro.
     */
    @Test
    void deveAtivarBarbeiro() {

        Usuario usuario = new Usuario(
                "Carlos",
                "carlos@email.com",
                "27999999999",
                "senha",
                Role.BARBEIRO
        );

        Barbeiro barbeiro =
                new Barbeiro(
                        usuario,
                        "Corte masculino",
                        false
                );

        when(barbeiroRepository.findById(1L))
                .thenReturn(Optional.of(barbeiro));

        when(barbeiroRepository.save(barbeiro))
                .thenReturn(barbeiro);

        BarbeiroResponseDTO resultado =
                barbeiroService.ativar(1L);

        // Depois de ativar, ativo deve ser true.
        assertTrue(resultado.getAtivo());

        verify(barbeiroRepository)
                .save(barbeiro);
    }
}
