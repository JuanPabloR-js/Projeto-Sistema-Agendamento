package br.unisales.sistema_agendamento.auth.dto;


import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @apiNote DTO responsável por retornar o token após o login.
 * @author Juan Pablo Rocha Hempel
 * @since 02.09.2026
 */
public record LoginResponseDTO(
        @Schema(
                description = "Token JWT utilizado nas requisições autenticadas",
                example = "eyJhbGciOiJIUzI1NiJ9..."
        )
        String token,

        String tipo,

        @Schema(
                description = "Identificador do usuário",
                example = "5"
        )
        Long usuarioId,

        @Schema(
                description = "Nome do usuário",
                example = "Corte de cabelo"
        )
        String nome,

        @Schema(
                description = "E-mail do usuário",
                example = "teste@email.com"
        )
        String email,

        String role

) {
}
