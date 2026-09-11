package br.unisales.sistema_agendamento.auth.controller;

import br.unisales.sistema_agendamento.auth.dto.LoginRequestDTO;
import br.unisales.sistema_agendamento.auth.dto.LoginResponseDTO;
import br.unisales.sistema_agendamento.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @apiNote Controller responsável pelos endpoints de autenticação.
 * @author Juan Pablo Rocha Hempel
 * @since 11.09.2026
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Operações de autenticação")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Realiza o login e devolve um token JWT")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }
}