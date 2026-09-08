package br.unisales.sistema_agendamento.usuario.dto;

import br.unisales.sistema_agendamento.usuario.model.Role;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        String telefone,
        Role role
) {
}