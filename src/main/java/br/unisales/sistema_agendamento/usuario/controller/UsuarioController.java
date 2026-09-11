package br.unisales.sistema_agendamento.usuario.controller;

import br.unisales.sistema_agendamento.usuario.dto.AtualizarUsuarioRequestDTO;
import br.unisales.sistema_agendamento.usuario.dto.TrocarSenhaRequestDTO;
import br.unisales.sistema_agendamento.usuario.dto.UsuarioResponseDTO;
import br.unisales.sistema_agendamento.usuario.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // Expõe endpoints HTTP da API.
@RequestMapping("/usuarios") // Prefixo das rotas deste controller.
public class UsuarioController {

    private final UsuarioService usuarioService;

    // O Spring injeta o service responsável pelas regras de usuário.
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Retorna os dados do usuário autenticado.
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> buscarMeuPerfil() {

        UsuarioResponseDTO response =
                usuarioService.buscarMeuPerfil();

        return ResponseEntity.ok(response);
    }

    // Recebe nome e telefone e atualiza o perfil autenticado.
    @PutMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> atualizarMeuPerfil(
            @RequestBody AtualizarUsuarioRequestDTO dto) {

        UsuarioResponseDTO response =
                usuarioService.atualizarMeuPerfil(dto);

        return ResponseEntity.ok(response);
    }

    // Recebe senha atual e nova senha e delega a troca ao service.
    @PutMapping("/me/senha")
    public ResponseEntity<Void> trocarSenha(
            @RequestBody TrocarSenhaRequestDTO dto) {

        usuarioService.trocarSenha(dto);

        // 204: operação realizada com sucesso, sem corpo de resposta.
        return ResponseEntity.noContent().build();
    }
}