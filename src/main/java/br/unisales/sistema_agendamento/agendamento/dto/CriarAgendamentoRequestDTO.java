package br.unisales.sistema_agendamento.agendamento.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * @apiNote DTO utilizado para receber os dados necessários na criação de um novo agendamento,
 *          contendo o barbeiro, o serviço escolhido, a data/hora desejada e uma observação opcional.
 * @author Juan Pablo Rocha Hempel
 * @since 17.09.2026
 */

public record CriarAgendamentoRequestDTO(

        @NotNull(message = "O barbeiro é obrigatório.")
        @Positive(message = "O ID do barbeiro deve ser positivo.")
        Long barbeiroId,

        @NotNull(message = "O serviço é obrigatório.")
        @Positive(message = "O ID do serviço deve ser positivo.")
        Long servicoId,

        @NotNull(message = "A data e o horário são obrigatórios.")
        @Future(message = "O agendamento deve ser realizado no futuro.")
        LocalDateTime dataHoraInicio,

        @Size(
                max = 500,
                message = "A observação deve possuir até 500 caracteres."
        )
        String observacao
) {
}