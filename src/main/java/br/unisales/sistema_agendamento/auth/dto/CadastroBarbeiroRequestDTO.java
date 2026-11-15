package br.unisales.sistema_agendamento.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Dados necessários para cadastrar um barbeiro.
 *
 * @author Juan Pablo Rocha Hempel
 * @since 15.11.2026
 */
public record CadastroBarbeiroRequestDTO(

        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 150, message = "O nome deve possuir no máximo 150 caracteres.")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 150, message = "O e-mail deve possuir no máximo 150 caracteres.")
        String email,

        @NotBlank(message = "A senha é obrigatória.")
        @Size(min = 6, max = 14, message = "A senha deve possuir entre 6 e 14 caracteres.")
        String senha,

        @NotBlank(message = "O telefone é obrigatório.")
        @Pattern(regexp = "\\d{10,11}", message = "O telefone deve possuir 10 ou 11 números.")
        String telefone,

        @NotBlank(message = "A especialidade é obrigatória.")
        String especialidade

) {}
