package br.unisales.sistema_agendamento.barbeiro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BarbeiroRequestDTO { // Dados entram na aplicação

    @NotNull (message = "O usuário é obrigatório.")
    private Long usuarioId;

    @NotBlank (message = "A especialidade é obrigatória.")
    private String especialidade; // Serve para não deixar o campo de especialidade vazio

    public BarbeiroRequestDTO(){
    }

    public BarbeiroRequestDTO (Long ususarioId, String especialidade){ // construtor
        this.usuarioId = usuarioId;
        this.especialidade = especialidade;
    }

    public Long getUsuarioId(){ // Getters e Setters
        return usuarioId;
    }

    public void setUsuarioId (Long usuarioID){
        this.usuarioId = usuarioId;
    }

    public String getEspecialidade(){
        return especialidade;
    }

    public void setEspecialidade (String especialidade){
        this.especialidade = especialidade;
    }
}
