package br.unisales.sistema_agendamento.usuario.service;

import br.unisales.sistema_agendamento.exception.ResourceNotFoundException;
import br.unisales.sistema_agendamento.usuario.dto.AtualizarUsuarioRequestDTO;
import br.unisales.sistema_agendamento.usuario.dto.UsuarioResponseDTO;
import br.unisales.sistema_agendamento.usuario.model.Usuario;
import br.unisales.sistema_agendamento.usuario.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario buscarEntidadePorId(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário não encontrado"));
    }

    public Usuario buscarEntidadePorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário não encontrado"));
    }

    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTelefone(),
                usuario.getRole()
        );
    }

    public UsuarioResponseDTO buscarMeuPerfil() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        Usuario usuario = buscarEntidadePorEmail(email);

        return toResponseDTO(usuario);
    }

    public UsuarioResponseDTO atualizarMeuPerfil(
            AtualizarUsuarioRequestDTO dto) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        Usuario usuario = buscarEntidadePorEmail(email);

        usuario.setNome(dto.nome());
        usuario.setTelefone(dto.telefone());

        usuarioRepository.save(usuario);

        return toResponseDTO(usuario);
    }

}


