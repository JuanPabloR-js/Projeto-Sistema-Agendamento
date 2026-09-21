package br.unisales.sistema_agendamento.agendamento.controller;

import br.unisales.sistema_agendamento.agendamento.service.AgendamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * @apiNote Consulta pública dos horários disponíveis para agendamento.
 * @author Juan Pablo Rocha Hempel
 * @since 02.09.2026
 */
@RestController
@RequestMapping("/barbeiros")
@Tag(name = "Horários disponíveis")
public class HorariosDisponiveisController {

    private final AgendamentoService service;

    public HorariosDisponiveisController(AgendamentoService service) {
        this.service = service;
    }

    @GetMapping("/{id}/horarios-disponiveis")
    @Operation(summary = "Consulta horários livres de um barbeiro")
    public ResponseEntity<List<String>> listarHorariosDisponiveis(
            @PathVariable("id") Long barbeiroId,

            @RequestParam("data")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate data,

            @RequestParam("servicoId") Long servicoId
    ) {
        return ResponseEntity.ok(
                service.listarHorariosDisponiveis(
                        barbeiroId,
                        data,
                        servicoId
                )
        );
    }
}