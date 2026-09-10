package br.unisales.sistema_agendamento.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * @apiNote DTO responsável por receber os dados de cadastro do cliente.
 * @author Juan Pablo Rocha Hempel
 * @since 02.09.2026
 */
public record CadastroClienteRequestDTO(

        @Schema(
                description = "Nome completo do cliente",
                example = "João Silva"
        )
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 150, message = "O nome deve possuir no máximo 150 caracteres.")
        String nome,

        @Schema(
                description = "Email do cliente",
                example = "joao@email.com"
        )
        @NotBlank(message = "O email é obrigatório.")
        @Email(message = "Informe um email válido.")
        String email,

        @Schema(
                description = "Senha do cliente",
                example = "123456"
        )
        @NotBlank(message = "A senha é obrigatória.")
        @Size(
                min = 6,
                max = 14,
                message = "A senha deve possuir entre 6 e 14 caracteres."
        )
        String senha,

        @Schema(
                description = "Telefone com DDD",
                example = "27999999999"
        )
        @NotBlank(message = "O telefone é obrigatório.")
        @Pattern(
                regexp = "\\d{10,11}",
                message = "O telefone deve possuir 10 ou 11 números."
        )
        String telefone

) {
}
