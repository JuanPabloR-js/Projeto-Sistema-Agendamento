package br.unisales.sistema_agendamento.servico;

import br.unisales.sistema_agendamento.servico.dto.ServicoRequest;
import br.unisales.sistema_agendamento.servico.dto.ServicoResponse;
import br.unisales.sistema_agendamento.servico.entity.Servico;
import br.unisales.sistema_agendamento.servico.repository.ServicoRepository;
import br.unisales.sistema_agendamento.servico.service.ServicoService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @apiNote Testes unitários das regras de negócio do domínio de serviços.
 * O ServicoRepository é simulado; nenhum banco de dados é utilizado.
 * @author Mateus Alves Costa
 * @since 25.10.2026
 */
@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock
    private ServicoRepository repository;

    @InjectMocks
    private ServicoService service;

    private Servico existente;
    private final Pageable pagina = PageRequest.of(0, 10);

    @BeforeEach
    void preparar() {
        existente = new Servico(
                "Corte masculino",
                "Corte com máquina e tesoura",
                30,
                new BigDecimal("45.00"),
                true
        );
        existente.setId(1L);
    }

    private ServicoRequest requisicao(String nome) {
        return new ServicoRequest(
                nome,
                "Barba com toalha quente",
                20,
                new BigDecimal("30.00"),
                true
        );
    }

    // Devolve a própria entidade salva, simulando o id gerado pelo banco.
    private void simularSave() {
        when(repository.save(any(Servico.class))).thenAnswer(invocacao -> {
            Servico salvo = invocacao.getArgument(0);
            if (salvo.getId() == null) {
                salvo.setId(10L);
            }
            return salvo;
        });
    }

    // ---------------------------------------------------------------- criar

    @Test
    void deveCriarServicoERetornarDadosSalvos() {
        when(repository.existsByNome("Barba")).thenReturn(false);
        simularSave();

        ServicoResponse resposta = service.criar(requisicao("Barba"));

        assertAll(
                () -> assertEquals(10L, resposta.id()),
                () -> assertEquals("Barba", resposta.nome()),
                () -> assertEquals("Barba com toalha quente", resposta.descricao()),
                () -> assertEquals(20, resposta.duracaoMinutos()),
                () -> assertEquals(new BigDecimal("30.00"), resposta.preco()),
                () -> assertTrue(resposta.ativo())
        );
    }

    @Test
    void deveRemoverEspacosDoNomeAoCriar() {
        when(repository.existsByNome(anyString())).thenReturn(false);
        simularSave();

        service.criar(requisicao("   Barba   "));

        ArgumentCaptor<Servico> captor = ArgumentCaptor.forClass(Servico.class);
        verify(repository).save(captor.capture());
        assertEquals("Barba", captor.getValue().getNome());
    }

    @Test
    void naoDeveCriarServicoComNomeJaCadastrado() {
        when(repository.existsByNome("Barba")).thenReturn(true);

        DataIntegrityViolationException erro = assertThrows(
                DataIntegrityViolationException.class,
                () -> service.criar(requisicao("Barba"))
        );

        assertEquals("Serviço com o mesmo nome já existe.", erro.getMessage());
        verify(repository, never()).save(any());
    }

    /*
     * O nome é salvo sem espaços, então a verificação de duplicidade
     * também precisa ignorá-los. Caso contrário " Barba " passa pela
     * checagem e só falha na constraint do banco.
     */
    @Test
    void naoDeveCriarServicoComNomeDuplicadoCercadoDeEspacos() {
        lenient().when(repository.existsByNome("Barba")).thenReturn(true);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> service.criar(requisicao("  Barba  "))
        );

        verify(repository, never()).save(any());
    }

    // ---------------------------------------------------------- buscarPorId

    @Test
    void deveBuscarServicoPorId() {
        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        ServicoResponse resposta = service.buscarPorId(1L);

        assertAll(
                () -> assertEquals(1L, resposta.id()),
                () -> assertEquals("Corte masculino", resposta.nome()),
                () -> assertEquals(30, resposta.duracaoMinutos())
        );
    }

    @Test
    void deveLancarErroAoBuscarServicoInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException erro = assertThrows(
                EntityNotFoundException.class,
                () -> service.buscarPorId(99L)
        );

        assertEquals("Serviço não encontrado com o ID: 99", erro.getMessage());
    }

    @Test
    void deveRetornarEntidadeUsadaPorOutrosModulos() {
        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        assertSame(existente, service.buscarEntidadePorId(1L));
    }

    @Test
    void deveLancarErroAoBuscarEntidadeInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> service.buscarEntidadePorId(99L)
        );
    }

    // --------------------------------------------------------------- listar

    @Test
    void deveListarServicosConvertidosParaResposta() {
        when(repository.findAll(pagina))
                .thenReturn(new PageImpl<>(List.of(existente), pagina, 1));

        Page<ServicoResponse> resposta = service.listar(pagina);

        assertEquals(1, resposta.getTotalElements());
        assertEquals("Corte masculino", resposta.getContent().get(0).nome());
    }

    @Test
    void deveRetornarPaginaVaziaQuandoNaoHouverServicos() {
        when(repository.findAll(pagina)).thenReturn(Page.empty(pagina));

        assertTrue(service.listar(pagina).isEmpty());
    }

    // --------------------------------------------------------------- buscar

    @Test
    @SuppressWarnings("unchecked")
    void deveFiltrarPorNomeSemDiferenciarMaiusculasEPorStatus() {
        when(repository.findAll(any(Specification.class), eq(pagina)))
                .thenReturn(new PageImpl<>(List.of(existente), pagina, 1));

        Page<ServicoResponse> resposta = service.buscar("CORTE", true, pagina);

        assertEquals(1, resposta.getTotalElements());

        // Executa o filtro montado pelo service para conferir as condições.
        ArgumentCaptor<Specification<Servico>> captor =
                ArgumentCaptor.forClass(Specification.class);
        verify(repository).findAll(captor.capture(), eq(pagina));

        Root<Servico> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Object> campoNome = mock(Path.class);
        Path<Object> campoAtivo = mock(Path.class);
        Expression<String> nomeMinusculo = mock(Expression.class);

        doReturn(campoNome).when(root).get("nome");
        doReturn(campoAtivo).when(root).get("ativo");
        doReturn(nomeMinusculo).when(cb).lower(any());

        captor.getValue().toPredicate(root, query, cb);

        verify(cb).like(nomeMinusculo, "%corte%");
        verify(cb).equal(campoAtivo, true);
    }

    @Test
    @SuppressWarnings("unchecked")
    void naoDeveAplicarFiltrosQuandoNomeEmBrancoEStatusNulo() {
        when(repository.findAll(any(Specification.class), eq(pagina)))
                .thenReturn(Page.empty(pagina));

        service.buscar("   ", null, pagina);

        ArgumentCaptor<Specification<Servico>> captor =
                ArgumentCaptor.forClass(Specification.class);
        verify(repository).findAll(captor.capture(), eq(pagina));

        Root<Servico> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        captor.getValue().toPredicate(root, mock(CriteriaQuery.class), cb);

        verify(cb, never()).like(any(), anyString());
        verify(cb, never()).equal(any(), any(Object.class));
    }

    // ------------------------------------------------------------ atualizar

    @Test
    void deveAtualizarTodosOsCamposDoServico() {
        when(repository.existsByNomeAndIdNot("Barba", 1L)).thenReturn(false);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        simularSave();

        ServicoResponse resposta = service.atualizar(1L, new ServicoRequest(
                "  Barba  ",
                "Barba com toalha quente",
                20,
                new BigDecimal("30.00"),
                false
        ));

        assertAll(
                () -> assertEquals(1L, resposta.id()),
                () -> assertEquals("Barba", resposta.nome()),
                () -> assertEquals("Barba com toalha quente", resposta.descricao()),
                () -> assertEquals(20, resposta.duracaoMinutos()),
                () -> assertEquals(new BigDecimal("30.00"), resposta.preco()),
                () -> assertFalse(resposta.ativo())
        );
    }

    @Test
    void devePermitirManterOProprioNomeAoAtualizar() {
        when(repository.existsByNomeAndIdNot("Corte masculino", 1L)).thenReturn(false);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        simularSave();

        ServicoResponse resposta =
                service.atualizar(1L, requisicao("Corte masculino"));

        assertEquals("Corte masculino", resposta.nome());
    }

    @Test
    void naoDeveAtualizarParaNomeDeOutroServico() {
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.existsByNomeAndIdNot("Barba", 1L)).thenReturn(true);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> service.atualizar(1L, requisicao("  Barba  "))
        );

        verify(repository, never()).save(any());
    }

    @Test
    void deveLancarErroAoAtualizarServicoInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> service.atualizar(99L, requisicao("Barba"))
        );

        verify(repository, never()).existsByNomeAndIdNot(anyString(), any());
        verify(repository, never()).save(any());
    }

    /*
     * Um id que não existe deve responder "não encontrado",
     * mesmo que o nome enviado pertença a outro serviço.
     */
    @Test
    void deveInformarServicoInexistenteAntesDeConflitoDeNome() {
        lenient().when(repository.existsByNomeAndIdNot("Barba", 99L)).thenReturn(true);
        lenient().when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> service.atualizar(99L, requisicao("Barba"))
        );
    }

    /*
     * O service só altera os campos informados (if != null),
     * então campos nulos devem manter os valores atuais.
     */
    @Test
    void deveManterValoresAtuaisQuandoCamposForemNulos() {
        lenient().when(repository.findById(1L)).thenReturn(Optional.of(existente));
        lenient().when(repository.save(any(Servico.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        ServicoResponse resposta = service.atualizar(
                1L,
                new ServicoRequest(null, null, null, null, null)
        );

        assertAll(
                () -> assertEquals("Corte masculino", resposta.nome()),
                () -> assertEquals("Corte com máquina e tesoura", resposta.descricao()),
                () -> assertEquals(30, resposta.duracaoMinutos()),
                () -> assertEquals(new BigDecimal("45.00"), resposta.preco()),
                () -> assertTrue(resposta.ativo())
        );
    }

    // ------------------------------------------------------ ativarDesativar

    @Test
    void deveDesativarServico() {
        when(repository.existsById(1L)).thenReturn(true);
        when(repository.getReferenceById(1L)).thenReturn(existente);

        service.ativarDesativar(1L, false);

        assertFalse(existente.getAtivo());
        verify(repository).save(existente);
    }

    @Test
    void deveAtivarServico() {
        existente.setAtivo(false);
        when(repository.existsById(1L)).thenReturn(true);
        when(repository.getReferenceById(1L)).thenReturn(existente);

        service.ativarDesativar(1L, true);

        assertTrue(existente.getAtivo());
        verify(repository).save(existente);
    }

    @Test
    void deveLancarErroAoAlterarStatusDeServicoInexistente() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThrows(
                EntityNotFoundException.class,
                () -> service.ativarDesativar(99L, true)
        );

        verify(repository, never()).getReferenceById(any());
        verify(repository, never()).save(any());
    }
}
