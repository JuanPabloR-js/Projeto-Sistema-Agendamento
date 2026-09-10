package br.unisales.sistema_agendamento.agendamento.domain;

import br.unisales.sistema_agendamento.agendamento.enumeration.StatusAgendamento;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * @apiNote Esta classe mapeia a tabela "tb_agendamento" no banco de dados,
 *          representando os agendamentos feitos por usuários na barbearia.
 * @author Juan Pablo Rocha Hempel
 * @since 02.09.2026
 */
@Entity
@Table(name = "tb_agendamento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Agendamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*@ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;*/

    /*@ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "barbeiro_id", nullable = false)
    private Barbeiro barbeiro;*/

    /*@ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;*/

    @Column(name = "data_hora_inicio", nullable = false)
    private LocalDateTime dataHoraInicio;

    @Column(name = "data_hora_fim", nullable = false)
    private LocalDateTime dataHoraFim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAgendamento status;

    @Column(length = 500)
    private String observacao;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @PrePersist
    public void antesDeSalvar(){
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }

        if (status == null) {
            status = StatusAgendamento.AGENDADO;
        }
    }

    public void cancelar() {
        this.status = StatusAgendamento.CANCELADO;
    }

    public void confirmar() {
        this.status = StatusAgendamento.CONFIRMADO;
    }

    public void concluir() {
        this.status = StatusAgendamento.CONCLUIDO;
    }
}
