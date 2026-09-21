package br.unisales.sistema_agendamento.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AtualizarUsuarioRequestDTO(

        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 2, max = 100,
                message = "O nome deve possuir entre 2 e 100 caracteres")
        String nome,

        @NotBlank(message = "O telefone é obrigatório")
        @Pattern(
                regexp = "[0-9]{10,11}",
                message = "Informe o telefone com DDD, contendo 10 ou 11 números"
        )
        String telefone
) {
}