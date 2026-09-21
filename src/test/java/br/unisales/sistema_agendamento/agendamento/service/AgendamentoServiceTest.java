package br.unisales.sistema_agendamento.agendamento.service;

import br.unisales.sistema_agendamento.agendamento.dto.AgendamentoResponseDTO;
import br.unisales.sistema_agendamento.agendamento.dto.CriarAgendamentoRequestDTO;
import br.unisales.sistema_agendamento.agendamento.entity.Agendamento;
import br.unisales.sistema_agendamento.agendamento.enumeration.StatusAgendamento;
import br.unisales.sistema_agendamento.agendamento.repository.AgendamentoRepository;
import br.unisales.sistema_agendamento.barbeiro.model.Barbeiro;
import br.unisales.sistema_agendamento.barbeiro.service.BarbeiroService;
import br.unisales.sistema_agendamento.cliente.domain.Cliente;
import br.unisales.sistema_agendamento.cliente.dto.ClienteResponseDTO;
import br.unisales.sistema_agendamento.cliente.service.ClienteService;
import br.unisales.sistema_agendamento.exception.BusinessException;
import br.unisales.sistema_agendamento.exception.ConflictException;
import br.unisales.sistema_agendamento.exception.ResourceNotFoundException;
import br.unisales.sistema_agendamento.servico.entity.Servico;
import br.unisales.sistema_agendamento.servico.service.ServicoService;
import br.unisales.sistema_agendamento.usuario.model.Role;
import br.unisales.sistema_agendamento.usuario.model.Usuario;
import br.unisales.sistema_agendamento.usuario.service.UsuarioService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @apiNote Testes das regras de criação, permissões, status e disponibilidade de agendamento.
 * @author Juan Pablo Rocha Hempel
 * @since 20.09.2026
 */
@ExtendWith(MockitoExtension.class)
class AgendamentoServiceAdicionalTest {

    @Mock
    private AgendamentoRepository repository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private ClienteService clienteService;

    @Mock
    private BarbeiroService barbeiroService;

    @Mock
    private ServicoService servicoService;

    private AgendamentoService service;

    private Usuario usuarioCliente;
    private Usuario usuarioBarbeiro;
    private Usuario administrador;

    private Cliente cliente;
    private Barbeiro barbeiro;
    private Servico servico;

    private static final LocalDate DIA = LocalDate.of(2030, 1, 7);
    private static final LocalDateTime AGORA = DIA.atTime(10, 0);

    @BeforeEach
    void preparar() {
        SecurityContextHolder.clearContext();

        Clock relogio = Clock.fixed(
                AGORA.toInstant(ZoneOffset.UTC),
                ZoneOffset.UTC
        );

        ExpedienteAgendamento expediente = new ExpedienteAgendamento(
                LocalTime.of(9, 0),
                LocalTime.of(12, 0),
                LocalTime.of(14, 0),
                LocalTime.of(18, 0),
                10,
                EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.SATURDAY)
        );

        service = new AgendamentoService(
                repository,
                usuarioService,
                clienteService,
                barbeiroService,
                servicoService,
                expediente,
                relogio
        );

        usuarioCliente = criarUsuario(1L, Role.CLIENTE);
        usuarioBarbeiro = criarUsuario(2L, Role.BARBEIRO);
        administrador = criarUsuario(3L, Role.ADMIN);

        cliente = new Cliente();
        cliente.setId(10L);
        cliente.setUsuario(usuarioCliente);
        cliente.setTelefone("27999999999");

        barbeiro = new Barbeiro(
                usuarioBarbeiro,
                "Corte e barba",
                true
        );
        barbeiro.setId(20L);

        servico = new Servico(
                "Corte",
                "Corte simples",
                40,
                new BigDecimal("40.00"),
                true
        );
        servico.setId(30L);
    }

    @AfterEach
    void limparAutenticacao() {
        // Evita que um teste deixe um usuário autenticado para o próximo.
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveCriarAgendamentoECalcularHorarioFinal() {
        prepararCriacaoComServico();
        permitirSalvar();

        LocalDateTime inicio = DIA.atTime(14, 0);

        AgendamentoResponseDTO resposta =
                service.criar(request(inicio));

        assertAll(
                () -> assertEquals(10L, resposta.clienteId()),
                () -> assertEquals(20L, resposta.barbeiroId()),
                () -> assertEquals(30L, resposta.servicoId()),
                () -> assertEquals(inicio, resposta.dataHoraInicio()),
                () -> assertEquals(
                        DIA.atTime(14, 40),
                        resposta.dataHoraFim()
                ),
                () -> assertEquals(
                        StatusAgendamento.AGENDADO,
                        resposta.status()
                ),
                () -> assertEquals(AGORA, resposta.dataCriacao())
        );

        // Confere a ordem: bloquear, consultar conflito e gravar.
        InOrder ordem = inOrder(repository);

        ordem.verify(repository).bloquearAgendaDoBarbeiro(20L);

        ordem.verify(repository).existeConflito(
                20L,
                inicio,
                inicio.plusMinutes(40),
                StatusAgendamento.CANCELADO
        );

        ordem.verify(repository).save(any(Agendamento.class));
    }

    @Test
    void naoDeveCriarSemAutenticacao() {
        assertThrows(
                BadCredentialsException.class,
                () -> service.criar(request(DIA.atTime(14, 0)))
        );

        verifyNoInteractions(repository);
    }

    @Test
    void naoDevePermitirQueBarbeiroCrieAgendamentoComoCliente() {
        autenticar(usuarioBarbeiro);

        assertThrows(
                AccessDeniedException.class,
                () -> service.criar(request(DIA.atTime(14, 0)))
        );

        verifyNoInteractions(clienteService, repository);
    }

    @Test
    void naoDeveCriarQuandoUsuarioNaoPossuiPerfilDeCliente() {
        autenticar(usuarioCliente);

        when(clienteService.buscarPorUsuarioId(1L))
                .thenThrow(
                        new ResourceNotFoundException("Cliente não encontrado.")
                );

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.criar(request(DIA.atTime(14, 0)))
        );

        verifyNoInteractions(repository);
    }

    @Test
    void naoDeveCriarNoPassado() {
        prepararCriacaoComServico();

        assertThrows(
                BusinessException.class,
                () -> service.criar(request(AGORA.minusMinutes(1)))
        );

        verify(repository, never()).save(any(Agendamento.class));
    }

    @Test
    void naoDeveCriarNoHorarioAtual() {
        prepararCriacaoComServico();

        assertThrows(
                BusinessException.class,
                () -> service.criar(request(AGORA))
        );

        verify(repository, never()).save(any(Agendamento.class));
    }

    @Test
    void naoDeveCriarQuandoAtendimentoUltrapassaOIntervalo() {
        prepararCriacaoComServico();

        // Começaria às 11:30 e terminaria às 12:10.
        assertThrows(
                BusinessException.class,
                () -> service.criar(request(DIA.atTime(11, 30)))
        );

        verify(repository, never()).save(any(Agendamento.class));
    }

    @Test
    void naoDeveCriarComBarbeiroInativo() {
        prepararCriacao();
        barbeiro.setAtivo(false);

        assertThrows(
                BusinessException.class,
                () -> service.criar(request(DIA.atTime(14, 0)))
        );

        verifyNoInteractions(servicoService);
        verify(repository, never()).save(any(Agendamento.class));
    }

    @Test
    void naoDeveCriarComServicoInativo() {
        prepararCriacaoComServico();
        servico.setAtivo(false);

        assertThrows(
                BusinessException.class,
                () -> service.criar(request(DIA.atTime(14, 0)))
        );

        verify(repository, never()).save(any(Agendamento.class));
    }



    @Test
    void devePermitirCancelamentoExatamenteDuasHorasAntes() {
        autenticar(usuarioCliente);

        Agendamento agendamento =
                agendamento(DIA.atTime(12, 0), StatusAgendamento.AGENDADO);

        prepararAlteracao(agendamento);
        permitirSalvar();

        AgendamentoResponseDTO resposta = service.cancelar(100L);

        assertEquals(StatusAgendamento.CANCELADO, resposta.status());
        verify(repository).save(agendamento);
    }

    @Test
    void naoDeveCancelarComMenosDeDuasHoras() {
        autenticar(usuarioCliente);

        Agendamento agendamento =
                agendamento(DIA.atTime(11, 59), StatusAgendamento.AGENDADO);

        prepararAlteracao(agendamento);

        assertThrows(
                BusinessException.class,
                () -> service.cancelar(100L)
        );

        assertEquals(StatusAgendamento.AGENDADO, agendamento.getStatus());
        verify(repository, never()).save(any(Agendamento.class));
    }

    @Test
    void naoDeveCancelarAgendamentoDeOutroCliente() {
        autenticar(criarUsuario(99L, Role.CLIENTE));

        Agendamento agendamento =
                agendamento(DIA.atTime(14, 0), StatusAgendamento.AGENDADO);

        prepararAlteracao(agendamento);

        assertThrows(
                AccessDeniedException.class,
                () -> service.cancelar(100L)
        );

        verify(repository, never()).save(any(Agendamento.class));
    }


    @Test
    void naoDeveCancelarAtendimentoConcluido() {
        autenticar(administrador);

        Agendamento agendamento =
                agendamento(DIA.atTime(9, 0), StatusAgendamento.CONCLUIDO);

        prepararAlteracao(agendamento);

        assertThrows(
                BusinessException.class,
                () -> service.cancelar(100L)
        );

        verify(repository, never()).save(any(Agendamento.class));
    }

    @Test
    void barbeiroResponsavelPodeConfirmar() {
        autenticar(usuarioBarbeiro);

        Agendamento agendamento =
                agendamento(DIA.atTime(14, 0), StatusAgendamento.AGENDADO);

        prepararAlteracao(agendamento);
        permitirSalvar();

        assertEquals(
                StatusAgendamento.CONFIRMADO,
                service.confirmar(100L).status()
        );
    }

    @Test
    void outroBarbeiroNaoPodeConfirmar() {
        autenticar(criarUsuario(99L, Role.BARBEIRO));

        Agendamento agendamento =
                agendamento(DIA.atTime(14, 0), StatusAgendamento.AGENDADO);

        prepararAlteracao(agendamento);

        assertThrows(
                AccessDeniedException.class,
                () -> service.confirmar(100L)
        );

        verify(repository, never()).save(any(Agendamento.class));
    }

    @Test
    void naoDeveConfirmarAgendamentoCancelado() {
        autenticar(usuarioBarbeiro);

        Agendamento agendamento =
                agendamento(DIA.atTime(14, 0), StatusAgendamento.CANCELADO);

        prepararAlteracao(agendamento);

        assertThrows(
                BusinessException.class,
                () -> service.confirmar(100L)
        );

        verify(repository, never()).save(any(Agendamento.class));
    }


    @Test
    void barbeiroResponsavelPodeConcluirDepoisDoFim() {
        autenticar(usuarioBarbeiro);

        Agendamento agendamento =
                agendamento(DIA.atTime(9, 0), StatusAgendamento.CONFIRMADO);

        prepararAlteracao(agendamento);
        permitirSalvar();

        assertEquals(
                StatusAgendamento.CONCLUIDO,
                service.concluir(100L).status()
        );
    }

    @Test
    void administradorPodeConcluirDepoisDoFim() {
        autenticar(administrador);

        Agendamento agendamento =
                agendamento(DIA.atTime(9, 0), StatusAgendamento.CONFIRMADO);

        prepararAlteracao(agendamento);
        permitirSalvar();

        assertEquals(
                StatusAgendamento.CONCLUIDO,
                service.concluir(100L).status()
        );
    }

    @Test
    void clienteNaoPodeConcluir() {
        autenticar(usuarioCliente);

        Agendamento agendamento =
                agendamento(DIA.atTime(9, 0), StatusAgendamento.CONFIRMADO);

        prepararAlteracao(agendamento);

        assertThrows(
                AccessDeniedException.class,
                () -> service.concluir(100L)
        );

        verify(repository, never()).save(any(Agendamento.class));
    }

    @Test
    void naoDeveConcluirAntesDoHorarioFinal() {
        autenticar(usuarioBarbeiro);

        Agendamento agendamento =
                agendamento(DIA.atTime(14, 0), StatusAgendamento.CONFIRMADO);

        prepararAlteracao(agendamento);

        assertThrows(
                BusinessException.class,
                () -> service.concluir(100L)
        );

        verify(repository, never()).save(any(Agendamento.class));
    }

    @Test
    void naoDeveConcluirSemConfirmacao() {
        autenticar(usuarioBarbeiro);

        Agendamento agendamento =
                agendamento(DIA.atTime(9, 0), StatusAgendamento.AGENDADO);

        prepararAlteracao(agendamento);

        assertThrows(
                BusinessException.class,
                () -> service.concluir(100L)
        );

        verify(repository, never()).save(any(Agendamento.class));
    }

    @Test
    void naoDeveConsultarAgendamentoDeOutroCliente() {
        autenticar(criarUsuario(99L, Role.CLIENTE));

        when(repository.findById(100L)).thenReturn(Optional.of(
                agendamento(DIA.atTime(14, 0), StatusAgendamento.AGENDADO)
        ));

        assertThrows(
                AccessDeniedException.class,
                () -> service.buscarPorId(100L)
        );
    }


    @Test
    void deveInformarAgendamentoInexistente() {
        autenticar(usuarioCliente);

        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.buscarPorId(999L)
        );
    }

    @Test
    void deveListarPeloUsuarioDoClienteAutenticado() {
        autenticar(usuarioCliente);

        when(repository.findByClienteUsuarioIdOrderByDataHoraInicioAsc(1L))
                .thenReturn(List.of(
                        agendamento(
                                DIA.atTime(14, 0),
                                StatusAgendamento.AGENDADO
                        )
                ));

        List<AgendamentoResponseDTO> resposta = service.listarMeus();

        assertEquals(1, resposta.size());
        assertEquals(10L, resposta.get(0).clienteId());

        verify(repository)
                .findByClienteUsuarioIdOrderByDataHoraInicioAsc(1L);
    }

    @Test
    void deveListarPeloUsuarioDoBarbeiroAutenticado() {
        autenticar(usuarioBarbeiro);

        when(repository.findByBarbeiroUsuarioIdOrderByDataHoraInicioAsc(2L))
                .thenReturn(List.of(
                        agendamento(
                                DIA.atTime(14, 0),
                                StatusAgendamento.AGENDADO
                        )
                ));

        assertEquals(1, service.listarMeus().size());

        verify(repository)
                .findByBarbeiroUsuarioIdOrderByDataHoraInicioAsc(2L);
    }

    @Test
    void clienteNaoPodeAcessarListagemAdministrativa() {
        autenticar(usuarioCliente);

        assertThrows(
                AccessDeniedException.class,
                () -> service.listarTodos(null, null, null)
        );

        verifyNoInteractions(repository);
    }


    private void prepararCriacao() {
        autenticar(usuarioCliente);

        ClienteResponseDTO perfil = ClienteResponseDTO.builder()
                .id(10L)
                .usuarioId(1L)
                .nome(usuarioCliente.getNome())
                .telefone("27999999999")
                .build();

        when(clienteService.buscarPorUsuarioId(1L))
                .thenReturn(perfil);

        when(clienteService.buscarEntidadePorId(10L))
                .thenReturn(cliente);

        when(repository.bloquearAgendaDoBarbeiro(20L))
                .thenReturn(Optional.of(20L));

        when(barbeiroService.buscarEntidadePorId(20L))
                .thenReturn(barbeiro);
    }

    private void prepararCriacaoComServico() {
        prepararCriacao();

        when(servicoService.buscarEntidadePorId(30L))
                .thenReturn(servico);
    }

    private void prepararAlteracao(Agendamento agendamento) {
        when(repository.buscarParaAlterar(100L))
                .thenReturn(Optional.of(agendamento));
    }

    private void permitirSalvar() {
        when(repository.save(any(Agendamento.class)))
                .thenAnswer(invocacao -> {
                    Agendamento salvo = invocacao.getArgument(0);

                    if (salvo.getId() == null) {
                        salvo.setId(100L);
                    }

                    return salvo;
                });
    }

    private void autenticar(Usuario usuario) {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(
                        usuario.getEmail(),
                        null,
                        "ROLE_" + usuario.getRole().name()
                )
        );

        when(usuarioService.buscarEntidadePorEmail(usuario.getEmail()))
                .thenReturn(usuario);
    }

    private Usuario criarUsuario(Long id, Role role) {
        Usuario usuario = new Usuario(
                "Usuário " + id,
                "usuario" + id + "@teste.com",
                "27999999999",
                "hash-simulado",
                role
        );

        usuario.setId(id);
        usuario.setAtivo(true);

        return usuario;
    }

    private CriarAgendamentoRequestDTO request(LocalDateTime inicio) {
        return new CriarAgendamentoRequestDTO(
                20L,
                30L,
                inicio,
                "Corte com tesoura."
        );
    }

    private Agendamento agendamento(
            LocalDateTime inicio,
            StatusAgendamento status
    ) {
        Agendamento agendamento = new Agendamento();
        agendamento.setId(100L);
        agendamento.setCliente(cliente);
        agendamento.setBarbeiro(barbeiro);
        agendamento.setServico(servico);
        agendamento.setDataHoraInicio(inicio);
        agendamento.setDataHoraFim(inicio.plusMinutes(40));
        agendamento.setStatus(status);
        agendamento.setDataCriacao(AGORA.minusDays(1));

        return agendamento;
    }
}