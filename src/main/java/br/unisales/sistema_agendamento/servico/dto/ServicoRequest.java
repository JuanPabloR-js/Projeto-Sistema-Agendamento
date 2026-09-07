package br.unisales.sistema_agendamento.servico.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record ServicoRequest(
    @NotBlank(message = "O nome do serviço não pode ser nulo ou vazio.") 
    String nome,

    @NotBlank(message = "A descrição do serviço não pode ser nula ou vazia.")
    String descricao,

    @NotBlank(message = "A duração do serviço não pode ser nula.")
    @PositiveOrZero(message = "A duração do serviço não pode ser negativa.")
    Integer duracaoMinutos,

    @DecimalMax(value = "1000.00", message = "O preço do serviço não pode ser maior que 1000.00.")
    BigDecimal preco
) {}