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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @apiNote Implementa as regras de negócio, permissões e disponibilidade dos agendamentos.
 * @author Juan Pablo Rocha Hempel
 * @since 20.09.2026
 */
@Service
@Transactional(readOnly = true)
public class AgendamentoService {

    private final AgendamentoRepository repository;
    private final UsuarioService usuarioService;
    private final ClienteService clienteService;
    private final BarbeiroService barbeiroService;
    private final ServicoService servicoService;
    private final ExpedienteAgendamento expediente;
    private final Clock relogio;

    public AgendamentoService(
            AgendamentoRepository repository,
            UsuarioService usuarioService,
            ClienteService clienteService,
            BarbeiroService barbeiroService,
            ServicoService servicoService,
            ExpedienteAgendamento expediente,
            @Qualifier("relogioAgendamento") Clock relogio
    ) {
        this.repository = repository;
        this.usuarioService = usuarioService;
        this.clienteService = clienteService;
        this.barbeiroService = barbeiroService;
        this.servicoService = servicoService;
        this.expediente = expediente;
        this.relogio = relogio;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AgendamentoResponseDTO criar(CriarAgendamentoRequestDTO dto) {
        Usuario usuario = usuarioAtual();
        exigirRole(usuario, Role.CLIENTE);

        validarId(dto.barbeiroId());
        validarId(dto.servicoId());

        // O cliente é identificado pelo usuário do token.
        ClienteResponseDTO perfil =
                clienteService.buscarPorUsuarioId(usuario.getId());

        Cliente cliente =
                clienteService.buscarEntidadePorId(perfil.getId());

        /*
         * Bloqueia a agenda antes de verificar conflitos.
         * A trava só é liberada ao terminar a transação.
         */
        repository.bloquearAgendaDoBarbeiro(dto.barbeiroId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Barbeiro não encontrado.")
                );

        Barbeiro barbeiro = buscarBarbeiroAtivo(dto.barbeiroId());
        Servico servico = buscarServicoAtivo(dto.servicoId());

        // Verifica a hora depois da possível espera pelo bloqueio.
        LocalDateTime agora = LocalDateTime.now(relogio);
        LocalDateTime inicio = dto.dataHoraInicio();

        if (inicio == null || !inicio.isAfter(agora)) {
            throw new BusinessException(
                    "O agendamento deve começar no futuro."
            );
        }

        int duracao = servico.getDuracaoMinutos();

        List<LocalDateTime> horariosPermitidos = expediente.listarInicios(
                inicio.toLocalDate(),
                duracao,
                agora
        );

        if (!horariosPermitidos.contains(inicio)) {
            throw new BusinessException(
                    "Horário fora do expediente ou da grade de horários."
            );
        }

        LocalDateTime fim = inicio.plusMinutes(duracao);

        boolean conflito = repository.existeConflito(
                barbeiro.getId(),
                inicio,
                fim,
                StatusAgendamento.CANCELADO
        );

        if (conflito) {
            throw new ConflictException(
                    "O barbeiro já possui atendimento neste período."
            );
        }

        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(cliente);
        agendamento.setBarbeiro(barbeiro);
        agendamento.setServico(servico);
        agendamento.setDataHoraInicio(inicio);
        agendamento.setDataHoraFim(fim);
        agendamento.setStatus(StatusAgendamento.AGENDADO);
        agendamento.setObservacao(dto.observacao());
        agendamento.setDataCriacao(agora);

        return toResponseDTO(repository.save(agendamento));
    }

    public AgendamentoResponseDTO buscarPorId(Long id) {
        Usuario usuario = usuarioAtual();
        validarId(id);

        Agendamento agendamento = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Agendamento não encontrado.")
                );

        if (usuario.getRole() != Role.ADMIN
                && !pertenceAoCliente(agendamento, usuario)
                && !pertenceAoBarbeiro(agendamento, usuario)) {

            throw new AccessDeniedException(
                    "Você não pode consultar este agendamento."
            );
        }

        return toResponseDTO(agendamento);
    }

    public List<AgendamentoResponseDTO> listarMeus() {
        Usuario usuario = usuarioAtual();
        List<Agendamento> agendamentos;

        if (usuario.getRole() == Role.CLIENTE) {
            agendamentos =
                    repository.findByClienteUsuarioIdOrderByDataHoraInicioAsc(
                            usuario.getId()
                    );
        } else if (usuario.getRole() == Role.BARBEIRO) {
            agendamentos =
                    repository.findByBarbeiroUsuarioIdOrderByDataHoraInicioAsc(
                            usuario.getId()
                    );
        } else {
            throw new AccessDeniedException(
                    "O administrador deve utilizar a listagem geral."
            );
        }

        return converterLista(agendamentos);
    }

    public List<AgendamentoResponseDTO> listarTodos(
            LocalDate data,
            StatusAgendamento status,
            Long barbeiroId
    ) {
        exigirRole(usuarioAtual(), Role.ADMIN);

        // Acrescenta apenas os filtros que foram informados.
        Specification<Agendamento> filtro =
                (root, query, cb) -> cb.conjunction();

        if (data != null) {
            filtro = filtro.and((root, query, cb) ->
                    cb.and(
                            cb.greaterThanOrEqualTo(
                                    root.<LocalDateTime>get("dataHoraInicio"),
                                    data.atStartOfDay()
                            ),
                            cb.lessThan(
                                    root.<LocalDateTime>get("dataHoraInicio"),
                                    data.plusDays(1).atStartOfDay()
                            )
                    )
            );
        }

        if (status != null) {
            filtro = filtro.and((root, query, cb) ->
                    cb.equal(root.get("status"), status)
            );
        }

        if (barbeiroId != null) {
            validarId(barbeiroId);

            filtro = filtro.and((root, query, cb) ->
                    cb.equal(root.get("barbeiro").get("id"), barbeiroId)
            );
        }

        List<Agendamento> agendamentos = repository.findAll(
                filtro,
                Sort.by("dataHoraInicio", "id")
        );

        return converterLista(agendamentos);
    }

    @Transactional
    public AgendamentoResponseDTO cancelar(Long id) {
        Usuario usuario = usuarioAtual();
        Agendamento agendamento = buscarParaAlterar(id);

        if (usuario.getRole() != Role.ADMIN) {
            if (!pertenceAoCliente(agendamento, usuario)) {
                throw new AccessDeniedException(
                        "Você só pode cancelar seus próprios agendamentos."
                );
            }

            LocalDateTime limite =
                    agendamento.getDataHoraInicio().minusHours(2);

            // Exatamente duas horas antes ainda é permitido.
            if (LocalDateTime.now(relogio).isAfter(limite)) {
                throw new BusinessException(
                        "Cancele com pelo menos duas horas de antecedência."
                );
            }
        }

        agendamento.cancelar();

        return toResponseDTO(repository.save(agendamento));
    }

    @Transactional
    public AgendamentoResponseDTO confirmar(Long id) {
        Usuario usuario = usuarioAtual();
        Agendamento agendamento = buscarParaAlterar(id);

        if (!pertenceAoBarbeiro(agendamento, usuario)) {
            throw new AccessDeniedException(
                    "Somente o barbeiro responsável pode confirmar."
            );
        }

        if (!agendamento.getDataHoraInicio()
                .isAfter(LocalDateTime.now(relogio))) {

            throw new BusinessException(
                    "Confirme o agendamento antes do horário de início."
            );
        }

        agendamento.confirmar();

        return toResponseDTO(repository.save(agendamento));
    }

    @Transactional
    public AgendamentoResponseDTO concluir(Long id) {
        Usuario usuario = usuarioAtual();
        Agendamento agendamento = buscarParaAlterar(id);

        if (usuario.getRole() != Role.ADMIN
                && !pertenceAoBarbeiro(agendamento, usuario)) {

            throw new AccessDeniedException(
                    "Somente o barbeiro responsável ou administrador pode concluir."
            );
        }

        if (LocalDateTime.now(relogio)
                .isBefore(agendamento.getDataHoraFim())) {

            throw new BusinessException(
                    "O horário final do atendimento ainda não chegou."
            );
        }

        agendamento.concluir();

        return toResponseDTO(repository.save(agendamento));
    }

    public List<String> listarHorariosDisponiveis(
            Long barbeiroId,
            LocalDate data,
            Long servicoId
    ) {
        validarId(barbeiroId);
        validarId(servicoId);

        if (data == null) {
            throw new BusinessException("Informe a data desejada.");
        }

        buscarBarbeiroAtivo(barbeiroId);
        Servico servico = buscarServicoAtivo(servicoId);

        int duracao = servico.getDuracaoMinutos();

        List<LocalDateTime> candidatos = expediente.listarInicios(
                data,
                duracao,
                LocalDateTime.now(relogio)
        );

        List<String> disponiveis = new ArrayList<>();

        if (candidatos.isEmpty()) {
            return disponiveis;
        }

        List<Agendamento> ocupados =
                repository.buscarOcupadosNoPeriodo(
                        barbeiroId,
                        data.atStartOfDay(),
                        data.plusDays(1).atStartOfDay(),
                        StatusAgendamento.CANCELADO
                );

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("HH:mm");

        for (LocalDateTime inicio : candidatos) {
            LocalDateTime fim = inicio.plusMinutes(duracao);
            boolean temConflito = false;

            for (Agendamento existente : ocupados) {
                if (inicio.isBefore(existente.getDataHoraFim())
                        && fim.isAfter(existente.getDataHoraInicio())) {

                    temConflito = true;
                    break;
                }
            }

            if (!temConflito) {
                disponiveis.add(inicio.format(formato));
            }
        }

        return disponiveis;
    }

    private Agendamento buscarParaAlterar(Long id) {
        validarId(id);

        return repository.buscarParaAlterar(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Agendamento não encontrado.")
                );
    }

    private Barbeiro buscarBarbeiroAtivo(Long id) {
        Barbeiro barbeiro = barbeiroService.buscarEntidadePorId(id);

        if (!barbeiro.getAtivo()
                || !barbeiro.getUsuario().isAtivo()) {

            throw new BusinessException("O barbeiro está inativo.");
        }

        return barbeiro;
    }

    private Servico buscarServicoAtivo(Long id) {
        Servico servico = servicoService.buscarEntidadePorId(id);

        if (!Boolean.TRUE.equals(servico.getAtivo())) {
            throw new BusinessException("O serviço está inativo.");
        }

        if (servico.getDuracaoMinutos() == null
                || servico.getDuracaoMinutos() <= 0) {

            throw new BusinessException(
                    "O serviço deve possuir duração positiva."
            );
        }

        return servico;
    }

    private Usuario usuarioAtual() {
        Authentication autenticacao =
                SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao == null
                || !autenticacao.isAuthenticated()
                || autenticacao instanceof AnonymousAuthenticationToken) {

            throw new BadCredentialsException(
                    "É necessário fazer login."
            );
        }

        Usuario usuario = usuarioService.buscarEntidadePorEmail(
                autenticacao.getName()
        );

        if (!usuario.isAtivo()) {
            throw new BadCredentialsException("Usuário inativo.");
        }

        return usuario;
    }

    private void exigirRole(Usuario usuario, Role role) {
        if (usuario.getRole() != role) {
            throw new AccessDeniedException(
                    "Você não possui permissão para esta operação."
            );
        }
    }

    private void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(
                    "O identificador deve ser positivo."
            );
        }
    }

    private boolean pertenceAoCliente(
            Agendamento agendamento,
            Usuario usuario
    ) {
        return usuario.getRole() == Role.CLIENTE
                && agendamento.getCliente().getUsuario()
                .getId().equals(usuario.getId());
    }

    private boolean pertenceAoBarbeiro(
            Agendamento agendamento,
            Usuario usuario
    ) {
        return usuario.getRole() == Role.BARBEIRO
                && agendamento.getBarbeiro().getUsuario()
                .getId().equals(usuario.getId());
    }

    private List<AgendamentoResponseDTO> converterLista(
            List<Agendamento> agendamentos
    ) {
        List<AgendamentoResponseDTO> respostas = new ArrayList<>();

        for (Agendamento agendamento : agendamentos) {
            respostas.add(toResponseDTO(agendamento));
        }

        return respostas;
    }

    private AgendamentoResponseDTO toResponseDTO(Agendamento a) {
        return new AgendamentoResponseDTO(
                a.getId(),
                a.getCliente().getId(),
                a.getCliente().getUsuario().getNome(),
                a.getBarbeiro().getId(),
                a.getBarbeiro().getUsuario().getNome(),
                a.getServico().getId(),
                a.getServico().getNome(),
                a.getDataHoraInicio(),
                a.getDataHoraFim(),
                a.getStatus(),
                a.getObservacao(),
                a.getDataCriacao()
        );
    }
}