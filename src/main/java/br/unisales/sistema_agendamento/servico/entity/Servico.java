package br.unisales.sistema_agendamento.servico.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @apiNote Esta classe mapeia a tabela "servico" no banco de dados,
 *          representando os serviços do sistema.
 * @author Mateus Alves Costa
 * @since 07.09.2026
 */

@Getter
@Setter 
@Entity 
@Table (name = "servico")
@NoArgsConstructor
public class Servico {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String nome;

    @Column(nullable = false, length = 255)
    private String descricao;

    @Column(name = "duracao_minutos", nullable = false)
    private Integer duracaoMinutos;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal preco;
    private Boolean ativo;

    @PrePersist
    void prePersist() {
        if (ativo == null) {
            ativo = true;
        }
    }

    public Servico(String nome, String descricao, Integer duracaoMinutos, BigDecimal preco, Boolean ativo) {
        this.nome = nome;
        this.descricao = descricao;
        this.duracaoMinutos = duracaoMinutos;
        this.preco = preco;
        this.ativo = ativo;
    }
}
