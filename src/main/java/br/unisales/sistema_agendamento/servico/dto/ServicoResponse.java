package br.unisales.sistema_agendamento.servico.dto;

import java.math.BigDecimal;

public record ServicoResponse(
    Long id,
    String nome,
    String descricao,
    Integer duracaoMinutos,
    BigDecimal preco,
    Boolean ativo
) {}
