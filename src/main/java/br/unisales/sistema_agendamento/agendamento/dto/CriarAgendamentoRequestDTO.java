package br.unisales.sistema_agendamento.agendamento.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @apiNote DTO utilizado para receber os dados necessários na criação de um novo agendamento,
 *          contendo o barbeiro, o serviço escolhido, a data/hora desejada e uma observação opcional.
 * @author Juan Pablo Rocha Hempel
 * @since 02.09.2026
 */

public record CriarAgendamentoRequestDTO(
        @NotNull(message = "O barbeiro é obrigatório.")
        Long barbeiroId,

        @NotNull(message = "O serviço é obrigatório.")
        Long servicoId,

        @NotNull(message = "A data e o horário são obr4igatórios.")
        @Future(message = "O agendamento deve ser realizado no futuro.")
        LocalDateTime dataHoraInicio,

        String observacao
) {



}
