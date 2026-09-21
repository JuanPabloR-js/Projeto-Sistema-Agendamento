package br.unisales.sistema_agendamento.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * @apiNote DTO responsável por receber os dados de login.
 * @author Juan Pablo Rocha Hempel
 * @since 02.09.2026
 */
public record LoginRequestDTO(

        @Schema(
                description = "Email utilizado pelo usuário",
                example = "cliente@email.com"
        )
        @NotBlank(message = "O email é obrigatório.")
        @Email(message = "Informe um email válido.")
        String email,

        @Schema(
                description = "Senha do usuário",
                example = "123456"
        )
        @NotBlank(message = "A senha é obrigatória.")
        String senha
) {
}
