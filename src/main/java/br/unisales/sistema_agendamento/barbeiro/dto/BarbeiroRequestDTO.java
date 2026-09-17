package br.unisales.sistema_agendamento.barbeiro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BarbeiroRequestDTO {

    // ID do usuário que será associado ao barbeiro.
    @NotNull(message = "O usuário é obrigatório.")
    private Long usuarioId;

    // Especialidade do barbeiro.
    @NotBlank(message = "A especialidade é obrigatória.")
    private String especialidade;

    // Construtor vazio.
    // É importante para frameworks como o Spring/Jackson.
    public BarbeiroRequestDTO() {
    }

    // Construtor usado para criar o DTO com os dados informados.
    public BarbeiroRequestDTO(
            Long usuarioId,
            String especialidade) {

        this.usuarioId = usuarioId;
        this.especialidade = especialidade;
    }

    // Retorna o ID do usuário.
    public Long getUsuarioId() {
        return usuarioId;
    }

    // Altera o ID do usuário.
    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    // Retorna a especialidade.
    public String getEspecialidade() {
        return especialidade;
    }

    // Altera a especialidade.
    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }
}

