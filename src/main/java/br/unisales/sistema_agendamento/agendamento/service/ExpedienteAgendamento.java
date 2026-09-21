package br.unisales.sistema_agendamento.agendamento.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @apiNote Calcula os horários possíveis dentro do expediente da barbearia.
 * @author Juan Pablo Rocha Hempel
 * @since 20.09.2026
 */
public class ExpedienteAgendamento {

    private final LocalTime abertura;
    private final LocalTime inicioIntervalo;
    private final LocalTime fimIntervalo;
    private final LocalTime fechamento;
    private final int passoMinutos;
    private final Set<DayOfWeek> dias;

    public ExpedienteAgendamento(
            LocalTime abertura,
            LocalTime inicioIntervalo,
            LocalTime fimIntervalo,
            LocalTime fechamento,
            int passoMinutos,
            Set<DayOfWeek> dias
    ) {
        if (!abertura.isBefore(inicioIntervalo)
                || !inicioIntervalo.isBefore(fimIntervalo)
                || !fimIntervalo.isBefore(fechamento)
                || passoMinutos < 1
                || passoMinutos > 1440
                || dias.isEmpty()) {

            throw new IllegalArgumentException(
                    "Configuração de expediente inválida."
            );
        }

        this.abertura = abertura;
        this.inicioIntervalo = inicioIntervalo;
        this.fimIntervalo = fimIntervalo;
        this.fechamento = fechamento;
        this.passoMinutos = passoMinutos;
        this.dias = Set.copyOf(dias);
    }

    public List<LocalDateTime> listarInicios(
            LocalDate data,
            int duracaoMinutos,
            LocalDateTime agora
    ) {
        if (duracaoMinutos <= 0) {
            throw new IllegalArgumentException(
                    "A duração deve ser positiva."
            );
        }

        List<LocalDateTime> horarios = new ArrayList<>();

        if (!dias.contains(data.getDayOfWeek())
                || data.isBefore(agora.toLocalDate())) {

            return horarios;
        }

        adicionarTurno(
                horarios, data, abertura, inicioIntervalo,
                duracaoMinutos, agora
        );

        adicionarTurno(
                horarios, data, fimIntervalo, fechamento,
                duracaoMinutos, agora
        );

        return horarios;
    }

    private void adicionarTurno(
            List<LocalDateTime> horarios,
            LocalDate data,
            LocalTime inicioTurno,
            LocalTime fimTurno,
            int duracaoMinutos,
            LocalDateTime agora
    ) {
        LocalDateTime inicio = data.atTime(inicioTurno);
        LocalDateTime limite = data.atTime(fimTurno);

        // O atendimento inteiro precisa caber no turno.
        while (!inicio.plusMinutes(duracaoMinutos).isAfter(limite)) {
            if (inicio.isAfter(agora)) {
                horarios.add(inicio);
            }

            inicio = inicio.plusMinutes(passoMinutos);
        }
    }
}