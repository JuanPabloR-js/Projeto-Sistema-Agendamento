package br.unisales.sistema_agendamento.usuario.controller;

import br.unisales.sistema_agendamento.usuario.dto.AtualizarUsuarioRequestDTO;
import br.unisales.sistema_agendamento.usuario.dto.TrocarSenhaRequestDTO;
import br.unisales.sistema_agendamento.usuario.dto.UsuarioResponseDTO;
import br.unisales.sistema_agendamento.usuario.service.UsuarioService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuários")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> buscarMeuPerfil() {
        return ResponseEntity.ok(usuarioService.buscarMeuPerfil());
    }

    @PutMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> atualizarMeuPerfil(
            @Valid @RequestBody AtualizarUsuarioRequestDTO dto) {

        return ResponseEntity.ok(usuarioService.atualizarMeuPerfil(dto));
    }

    @PutMapping("/me/senha")
    public ResponseEntity<Void> trocarSenha(
            @Valid @RequestBody TrocarSenhaRequestDTO dto) {

        usuarioService.trocarSenha(dto);

        return ResponseEntity.noContent().build();
    }
}