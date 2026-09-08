package br.unisales.sistema_agendamento.servico.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.unisales.sistema_agendamento.servico.dto.ServicoRequest;
import br.unisales.sistema_agendamento.servico.dto.ServicoResponse;
import br.unisales.sistema_agendamento.servico.service.ServicoService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/servicos")
public class ServicoController {
    private final ServicoService service;

    /**
     * @apiNote Método responsável por criar um novo serviço.
     * @param request --> Dados do serviço a ser criado.
     * @return --> Retorna o serviço criado.
     * @author Mateus Alves Costa
     * @since 07.09.2026
     */
    @PostMapping
    public ResponseEntity<ServicoResponse> criar(@RequestBody ServicoRequest request) {
        ServicoResponse response = service.criar(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    /**
     * @apiNote Método responsável por buscar um serviço pelo seu ID.
     * @param id --> ID do serviço a ser buscado.
     * @return --> Retorna um objeto ServicoResponse contendo os dados do serviço
     *         encontrado.
     * @author Mateus Alves Costa
     * @since 07.09.2026
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponse> buscarPorId(@PathVariable Long id) {
        ServicoResponse response = service.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * @apiNote Método responsável por receber os parâmetros da busca de serviços no
     *          sistema
     * @param nome     --> Nome do serviço a ser filtrado.
     * @param ativo    --> Status de atividade do serviço a ser filtrado.
     * @param pageable --> Configuração de paginação.
     * @return --> Retorna uma página com os dados dos serviços encontrados.
     */
    @GetMapping("/buscar")
    public ResponseEntity<Page<ServicoResponse>> buscar(@RequestParam(required = false) String nome,
            @RequestParam(required = false) Boolean ativo,
            @PageableDefault(size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(service.buscar(nome, ativo, pageable));
    }

    /**
     * @apiNote Método responsável por listar todos os serviços cadastrados no
     *          sistema.
     * @param pageable --> Configuração de paginação.
     * @return --> Retorna uma página de objetos ServicoResponse.
     * @author Mateus Alves Costa
     * @since 07.09.2026
     */
    @GetMapping
    public ResponseEntity<Page<ServicoResponse>> listar(
            @PageableDefault(size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<ServicoResponse> response = service.listar(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * @apiNote Método responsável por atualizar os dados de um serviço existente.
     * @param id      --> ID do serviço a ser atualizado.
     * @param request --> Dados atualizados do serviço.
     * @return --> Retorna o serviço atualizado.
     * @author Mateus Alves Costa
     * @since 07.09.2026
     */
    @PutMapping("/{id}")
    public ResponseEntity<ServicoResponse> atualizar(@PathVariable Long id, @RequestBody ServicoRequest request) {
        ServicoResponse response = service.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * @apiNote Método responsável por ativar ou desativar um serviço.
     * @param id    --> ID do serviço a ser ativado ou desativado.
     * @param valor --> Valor booleano indicando se o serviço deve ser ativado
     *              (true) ou desativado (false).
     * @return --> Retorna uma resposta HTTP sem conteúdo.
     * @author Mateus Alves Costa
     * @since 07.09.2026
     */
    @PatchMapping("/{id}/{valor}")
    public ResponseEntity<Void> ativarDesativar(@PathVariable Long id, @PathVariable Boolean valor) {
        service.ativarDesativar(id, valor);
        return ResponseEntity.noContent().build();
    }

}
