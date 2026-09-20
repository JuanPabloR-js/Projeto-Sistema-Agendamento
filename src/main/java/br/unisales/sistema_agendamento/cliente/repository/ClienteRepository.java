// Feito por: moacyr dev2

package br.unisales.sistema_agendamento.cliente.repository;


import br.unisales.sistema_agendamento.cliente.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);
}
