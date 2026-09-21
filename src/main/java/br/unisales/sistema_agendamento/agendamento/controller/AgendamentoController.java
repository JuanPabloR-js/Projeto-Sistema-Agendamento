package br.unisales.sistema_agendamento.agendamento.controller;

import br.unisales.sistema_agendamento.agendamento.dto.AgendamentoResponseDTO;
import br.unisales.sistema_agendamento.agendamento.dto.CriarAgendamentoRequestDTO;
import br.unisales.sistema_agendamento.agendamento.enumeration.StatusAgendamento;
import br.unisales.sistema_agendamento.agendamento.service.AgendamentoService;
import br.unisales.sistema_agendamento.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/**
 * @apiNote Expõe as operações HTTP do módulo de agendamento.
 * @author Juan Pablo Rocha Hempel
 * @since 20.09.2026
 */
@RestController
@RequestMapping("/agendamentos")
@Tag(name = "Agendamentos")
@SecurityRequirement(name = OpenApiConfig.JWT_SECURITY_SCHEME)
public class AgendamentoController {

    private final AgendamentoService service;

    public AgendamentoController(AgendamentoService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cliente cria seu agendamento")
    public ResponseEntity<AgendamentoResponseDTO> criar(
            @Valid @RequestBody CriarAgendamentoRequestDTO dto
    ) {
        AgendamentoResponseDTO resposta = service.criar(dto);

        return ResponseEntity
                .created(URI.create("/agendamentos/" + resposta.id()))
                .body(resposta);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta um agendamento acessível ao usuário")
    public ResponseEntity<AgendamentoResponseDTO> buscarPorId(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/meus")
    @Operation(summary = "Lista os agendamentos do usuário autenticado")
    public ResponseEntity<List<AgendamentoResponseDTO>> listarMeus() {
        return ResponseEntity.ok(service.listarMeus());
    }

    @GetMapping
    @Operation(summary = "Administrador lista agendamentos com filtros")
    public ResponseEntity<List<AgendamentoResponseDTO>> listarTodos(
            @RequestParam(value = "data", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate data,

            @RequestParam(value = "status", required = false)
            StatusAgendamento status,

            @RequestParam(value = "barbeiroId", required = false)
            Long barbeiroId
    ) {
        return ResponseEntity.ok(
                service.listarTodos(data, status, barbeiroId)
        );
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancela um agendamento")
    public ResponseEntity<AgendamentoResponseDTO> cancelar(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(service.cancelar(id));
    }

    @PatchMapping("/{id}/confirmar")
    @Operation(summary = "Barbeiro responsável confirma o agendamento")
    public ResponseEntity<AgendamentoResponseDTO> confirmar(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(service.confirmar(id));
    }

    @PatchMapping("/{id}/concluir")
    @Operation(summary = "Conclui um atendimento confirmado")
    public ResponseEntity<AgendamentoResponseDTO> concluir(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(service.concluir(id));
    }
}