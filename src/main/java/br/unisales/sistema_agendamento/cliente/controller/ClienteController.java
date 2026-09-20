// Feito por: moacyr dev2

package br.unisales.sistema_agendamento.cliente.controller;


import br.unisales.sistema_agendamento.cliente.dto.ClienteRequestDTO;
import br.unisales.sistema_agendamento.cliente.dto.ClienteResponseDTO;
import br.unisales.sistema_agendamento.cliente.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> criar(
            @Valid @RequestBody ClienteRequestDTO dto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(clienteService.criar(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> buscarPorId(
            @PathVariable("id") Long clienteId
    ) {
        return ResponseEntity.ok(clienteService.buscarPorId(clienteId));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<ClienteResponseDTO> buscarPorUsuarioId(
            @PathVariable Long usuarioId
    ) {
        return ResponseEntity.ok(
                clienteService.buscarPorUsuarioId(usuarioId)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> atualizar(
            @PathVariable("id") Long clienteId,
            @Valid @RequestBody ClienteRequestDTO dto
    ) {
        return ResponseEntity.ok(clienteService.atualizar(clienteId, dto));
    }
}
