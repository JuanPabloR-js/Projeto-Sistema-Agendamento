// Feito por: moacyr dev2

package br.unisales.sistema_agendamento.cliente.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteResponseDTO {

    private Long id;
    private Long usuarioId;
    private String nome;
    private String telefone;
}
