package br.unisales.sistema_agendamento.servico.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/*
 * Os limites de tamanho seguem as colunas da tabela "servico".
 */
public record ServicoRequest(
    @NotBlank(message = "O nome do serviço não pode ser nulo ou vazio.") 
    @Size(max = 100, message = "O nome do serviço deve possuir no máximo 100 caracteres.")
    String nome,

    @NotBlank(message = "A descrição do serviço não pode ser nula ou vazia.")
    @Size(max = 255, message = "A descrição do serviço deve possuir no máximo 255 caracteres.")
    String descricao,

    // O agendamento não aceita serviços com duração zero.
    @NotNull(message = "A duração do serviço não pode ser nula.")
    @Positive(message = "A duração do serviço deve ser maior que zero.")
    Integer duracaoMinutos,

    @NotNull(message = "O preço do serviço não pode ser nulo.")
    @PositiveOrZero(message = "O preço do serviço não pode ser negativo.")
    @DecimalMax(value = "1000.00", message = "O preço do serviço não pode ser maior que 1000.00.")
    @Digits(integer = 4, fraction = 2, message = "O preço do serviço deve possuir no máximo duas casas decimais.")
    BigDecimal preco,

    @NotNull(message = "O status do serviço não pode ser nulo.")
    Boolean ativo
) {}