package br.unisales.sistema_agendamento.usuario.dto;

public record TrocarSenhaRequestDTO(
        String senhaAtual,
        String novaSenha
) {
}