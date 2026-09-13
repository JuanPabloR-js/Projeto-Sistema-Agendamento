package br.unisales.sistema_agendamento.barbeiro.model;

import br.unisales.sistema_agendamento.usuario.model.Usuario; // importo a classe usuario
import jakarta.persistence.*; // importa as anotações do JPA

@Entity // entidade do banco de dados - cria uma tabela chamada - barbeiros
@Table (name = "barbeiros")

public class Barbeiro {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne // relacionamento de 1 usuario pra 1 barbeiro
    @JoinColumn (name = "usuario_id", nullable = false, unique = true) // impede que os usuarios registrem o corte de cabelo com dois barbeiros
    private Usuario usuario;

    @Column (nullable = false)
    private String especialidade;

    @Column (nullable = false)
    private boolean ativo; // verifica status de ativo do barbeiro

    public Barbeiro(){
    }

    public Barbeiro (Usuario usuario, String especialidade, Boolean ativo){ // Construtor
        this.usuario = usuario;
        this.especialidade = especialidade;
        this.ativo = ativo;
    }

    public Long getId(){
        return id;
    }

    public void setId (Long id){
        this.id = id;
    }

    public Usuario getUsuario(){
        return usuario;
    }

    public void setUsuario (Usuario usuario){
        this.usuario = usuario;
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
