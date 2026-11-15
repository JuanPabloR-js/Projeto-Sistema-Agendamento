package br.unisales.sistema_agendamento.auth.controller;

import br.unisales.sistema_agendamento.auth.dto.CadastroClienteRequestDTO;
import br.unisales.sistema_agendamento.cliente.dto.ClienteResponseDTO;
import org.springframework.http.HttpStatus;
import br.unisales.sistema_agendamento.auth.dto.LoginRequestDTO;
import br.unisales.sistema_agendamento.auth.dto.LoginResponseDTO;
import br.unisales.sistema_agendamento.auth.dto.CadastroBarbeiroRequestDTO;
import br.unisales.sistema_agendamento.barbeiro.dto.BarbeiroResponseDTO;
import br.unisales.sistema_agendamento.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

    @PostMapping("/cadastro/cliente")
    @Operation(summary = "Cria a conta e o perfil do cliente")
    public ResponseEntity<ClienteResponseDTO> cadastrar(
            @Valid @RequestBody CadastroClienteRequestDTO request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.cadastrar(request));
    }

    /**
     * POST /auth/cadastro/barbeiro
     *
     * Cadastra um novo barbeiro.
     * Somente ADMIN pode executar essa operação.
     */
    @PostMapping("/cadastro/barbeiro")
    @SecurityRequirement(name = OpenApiConfig.JWT_SECURITY_SCHEME)
    public ResponseEntity<BarbeiroResponseDTO> cadastrarBarbeiro(
            @Valid @RequestBody CadastroBarbeiroRequestDTO dto) {

        BarbeiroResponseDTO barbeiroCriado =
                authService.cadastrarBarbeiro(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(barbeiroCriado);
    }
}