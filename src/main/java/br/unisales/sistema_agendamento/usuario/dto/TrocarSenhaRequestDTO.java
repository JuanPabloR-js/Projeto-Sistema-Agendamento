package br.unisales.sistema_agendamento.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TrocarSenhaRequestDTO(

        @NotBlank(message = "A senha atual é obrigatória")
        String senhaAtual,

        @NotBlank(message = "A nova senha é obrigatória")
        @Size(min = 8, max = 64,
                message = "A nova senha deve possuir entre 8 e 64 caracteres")
        String novaSenha
) {
}