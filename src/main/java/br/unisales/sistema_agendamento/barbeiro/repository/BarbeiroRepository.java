package br.unisales.sistema_agendamento.barbeiro.repository;
// Faz a comunicação da entidade Barbeiro com o banco de dados - acessa o banco

import br.unisales.sistema_agendamento.barbeiro.model.Barbeiro; // Importa a entidade barbeiro
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// BarbeiroRepository é um repositoryJPA responsável pela entidade Barbeiro, cuja chave primária é Long
public interface BarbeiroRepository extends JpaRepository<Barbeiro, Long>{
    Optional<Barbeiro> findByUsuarioId (Long usuarioId);
}


