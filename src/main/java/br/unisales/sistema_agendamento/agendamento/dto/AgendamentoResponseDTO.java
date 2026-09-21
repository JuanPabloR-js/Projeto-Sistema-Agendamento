package br.unisales.sistema_agendamento.agendamento.dto;

import br.unisales.sistema_agendamento.agendamento.enumeration.StatusAgendamento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * @apiNote DTO responsável por retornar os dados de um agendamento.
 * @author Juan Pablo Rocha Hempel
 * @since 02.09.2026
 */
@Schema(description = "Dados retornados de um agendamento")
public record AgendamentoResponseDTO(

        @Schema(
                description = "Identificador do agendamento",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Identificador do cliente",
                example = "5"
        )
        Long clienteId,

        @Schema(
                description = "Nome do cliente",
                example = "João Silva"
        )
        String clienteNome,

        @Schema(
                description = "Identificador do barbeiro",
                example = "2"
        )
        Long barbeiroId,

        @Schema(
                description = "Nome do barbeiro",
                example = "Carlos Souza"
        )
        String barbeiroNome,

        @Schema(
                description = "Identificador do serviço",
                example = "3"
        )
        Long servicoId,

        @Schema(
                description = "Nome do serviço",
                example = "Corte de cabelo"
        )
        String servicoNome,

        @Schema(
                description = "Data e horário de início",
                example = "2026-09-15T14:00:00"
        )
        LocalDateTime dataHoraInicio,

        @Schema(
                description = "Data e horário calculado para o fim",
                example = "2026-09-15T14:40:00"
        )
        LocalDateTime dataHoraFim,

        @Schema(
                description = "Status atual do agendamento",
                example = "AGENDADO"
        )
        StatusAgendamento status,

        @Schema(
                description = "Observação informada pelo cliente",
                example = "Prefiro corte com tesoura."
        )
        String observacao,

        @Schema(
                description = "Data de criação do agendamento",
                example = "2026-09-10T15:30:00"
        )
        LocalDateTime dataCriacao

) {
}