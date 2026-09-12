package br.unisales.sistema_agendamento.barbeiro.dto;

public class BarbeiroResponseDTO { // Dados saem da aplicacão

    private Long id;
    private Long usuarioId;
    private String nome;
    private String especialidade;
    private boolean ativo;

    public BarbeiroResponseDTO(){
    }

    public BarbeiroResponseDTO( // Construtor
            Long id,
            Long usuarioId,
            String nome,
            String especialidade,
            boolean ativo){

        this.id = id;
        this.usuarioId = usuarioId;
        this.nome = nome;
        this.especialidade = especialidade;
        this.ativo = ativo;
    }

    public Long getId(){ // Getters e Setters
        return id;
    }

    public void setId (Long id){
        this.id = id;
    }

    public Long getUsuarioId(){
        return usuarioId;
    }

    public void setUsuarioId (Long usuarioid){
        this.usuarioId = usuarioId;
    }

    public String getNome(){
        return nome;
    }

    public void setNome (String nome){
        this.nome = nome;
    }

    public String getEspecialidade(){
        return especialidade;
    }

    public void setEspecialidade (String especialidade){
        this.especialidade = especialidade;
    }

    public boolean getAtivo(){
        return ativo;
    }

    public void setAtivo (boolean ativo){
        this.ativo = ativo;
    }
}
