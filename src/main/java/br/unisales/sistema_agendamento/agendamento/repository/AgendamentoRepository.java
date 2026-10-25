package br.unisales.sistema_agendamento.agendamento.repository;

import br.unisales.sistema_agendamento.agendamento.entity.Agendamento;
import br.unisales.sistema_agendamento.agendamento.enumeration.StatusAgendamento;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @apiNote Consultas e bloqueios utilizados pelo módulo de agendamento.
 * @author Juan Pablo Rocha Hempel
 * @since 20.09.2026
 */
public interface AgendamentoRepository
        extends JpaRepository<Agendamento, Long>,
        JpaSpecificationExecutor<Agendamento> {

    /*
     * Bloqueia a linha do barbeiro até a transação terminar.
     * Duas criações para o mesmo barbeiro aguardam uma à outra.
     */
    @Query(
            value = """
                    SELECT id
                    FROM barbeiros
                    WHERE id = :id
                    FOR UPDATE
                    """,
            nativeQuery = true
    )
    Optional<Long> bloquearAgendaDoBarbeiro(@Param("id") Long id);

    // Mesma ideia, para a agenda do cliente.
    @Query(
            value = """
                    SELECT id
                    FROM tb_cliente
                    WHERE id = :id
                    FOR UPDATE
                    """,
            nativeQuery = true
    )
    Optional<Long> bloquearAgendaDoCliente(@Param("id") Long id);

    // Evita alterações simultâneas no mesmo agendamento.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Agendamento a WHERE a.id = :id")
    Optional<Agendamento> buscarParaAlterar(@Param("id") Long id);

    /*
     * Os limites são exclusivos:
     * começar exatamente quando outro termina é permitido.
     */
    @Query("""
            SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
            FROM Agendamento a
            WHERE a.barbeiro.id = :barbeiroId
              AND a.status <> :cancelado
              AND a.dataHoraInicio < :fim
              AND a.dataHoraFim > :inicio
            """)
    boolean existeConflito(
            @Param("barbeiroId") Long barbeiroId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("cancelado") StatusAgendamento cancelado
    );

    // Um cliente não pode estar em dois atendimentos ao mesmo tempo.
    @Query("""
            SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
            FROM Agendamento a
            WHERE a.cliente.id = :clienteId
              AND a.status <> :cancelado
              AND a.dataHoraInicio < :fim
              AND a.dataHoraFim > :inicio
            """)
    boolean existeConflitoDoCliente(
            @Param("clienteId") Long clienteId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("cancelado") StatusAgendamento cancelado
    );

    @Query("""
            SELECT a
            FROM Agendamento a
            WHERE a.barbeiro.id = :barbeiroId
              AND a.status <> :cancelado
              AND a.dataHoraInicio < :fim
              AND a.dataHoraFim > :inicio
            ORDER BY a.dataHoraInicio
            """)
    List<Agendamento> buscarOcupadosNoPeriodo(
            @Param("barbeiroId") Long barbeiroId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("cancelado") StatusAgendamento cancelado
    );

    List<Agendamento> findByClienteUsuarioIdOrderByDataHoraInicioAsc(
            Long usuarioId
    );

    List<Agendamento> findByBarbeiroUsuarioIdOrderByDataHoraInicioAsc(
            Long usuarioId
    );
}