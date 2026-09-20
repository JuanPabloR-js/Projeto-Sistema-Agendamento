// Feito por: moacyr dev2

package br.unisales.sistema_agendamento.cliente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteRequestDTO {

    @NotNull(message = "O usuário é obrigatório.")
    private Long usuarioId;

    @NotBlank(message = "O telefone é obrigatório.")
    private String telefone;
}
