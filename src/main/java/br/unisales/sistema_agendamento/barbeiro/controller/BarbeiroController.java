package br.unisales.sistema_agendamento.barbeiro.controller;

import br.unisales.sistema_agendamento.barbeiro.dto.BarbeiroRequestDTO;
import br.unisales.sistema_agendamento.barbeiro.dto.BarbeiroResponseDTO;
import br.unisales.sistema_agendamento.barbeiro.service.BarbeiroService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * @RestController informa ao Spring que esta classe será responsável
 * por receber requisições HTTP e devolver respostas para a API.
 */
@RestController

/*
 * Define o caminho base de todos os endpoints deste controller.
 *
 * Então tudo que estiver aqui começará com:
 *
 * /barbeiros
 */
@RequestMapping("/barbeiros")
public class BarbeiroController {

    /*
     * Service responsável pelas regras de negócio do Barbeiro.
     *
     * O Controller não acessa o Repository diretamente.
     *
     * Fluxo:
     *
     * Controller -> Service -> Repository -> Banco
     */
    private final BarbeiroService barbeiroService;

    /*
     * Construtor usado pelo Spring para injetar o BarbeiroService.
     */
    public BarbeiroController(BarbeiroService barbeiroService) {
        this.barbeiroService = barbeiroService;
    }

    /*
     * GET /barbeiros
     *
     * Lista todos os barbeiros cadastrados.
     *
     * Exemplo:
     *
     * http://localhost:8080/barbeiros
     */
    @GetMapping
    public ResponseEntity<List<BarbeiroResponseDTO>> listarTodos() {

        /*
         * Chama o Service para buscar todos os barbeiros.
         */
        List<BarbeiroResponseDTO> barbeiros =
                barbeiroService.listarTodos();

        /*
         * ResponseEntity.ok(...)
         *
         * devolve status HTTP 200 OK
         * junto com a lista de barbeiros.
         */
        return ResponseEntity.ok(barbeiros);
    }

    /*
     * GET /barbeiros/{id}
     *
     * Busca apenas um barbeiro pelo ID.
     *
     * Exemplo:
     *
     * GET /barbeiros/5
     */
    @GetMapping("/{id}")
    public ResponseEntity<BarbeiroResponseDTO> buscarPorId(

            /*
             * @PathVariable pega o valor que veio na URL.
             *
             * Exemplo:
             *
             * /barbeiros/5
             *
             * id = 5
             */
            @PathVariable Long id) {

        /*
         * Chama o Service e busca o barbeiro.
         */
        BarbeiroResponseDTO barbeiro =
                barbeiroService.buscarPorId(id);

        /*
         * Retorna:
         *
         * HTTP 200 OK
         *
         * junto com os dados do barbeiro.
         */
        return ResponseEntity.ok(barbeiro);
    }

    /*
     * POST /barbeiros
     *
     * Cria um novo perfil de barbeiro.
     */
    @PostMapping
    public ResponseEntity<BarbeiroResponseDTO> criar(

            /*
             * @RequestBody:
             *
             * pega o JSON enviado pelo cliente e transforma
             * em um objeto BarbeiroRequestDTO.
             *
             * @Valid:
             *
             * executa as validações definidas dentro do DTO,
             * como @NotNull e @NotBlank.
             */
            @Valid
            @RequestBody
            BarbeiroRequestDTO dto) {

        /*
         * O Controller envia os dados para o Service.
         *
         * O Service fica responsável por:
         *
         * - buscar o usuário;
         * - verificar se ele possui role BARBEIRO;
         * - verificar se já possui perfil;
         * - criar o barbeiro;
         * - salvar no banco.
         */
        BarbeiroResponseDTO barbeiroCriado =
                barbeiroService.criar(dto);

        /*
         * Retorna HTTP 200 OK com o barbeiro criado.
         *
         * Mais adiante poderíamos utilizar HTTP 201 CREATED,
         * mas por enquanto esta implementação funciona corretamente.
         */
        return ResponseEntity.ok(barbeiroCriado);
    }

    /*
     * PUT /barbeiros/{id}
     *
     * Atualiza os dados de um barbeiro.
     *
     * Exemplo:
     *
     * PUT /barbeiros/3
     */
    @PutMapping("/{id}")
    public ResponseEntity<BarbeiroResponseDTO> atualizar(

            /*
             * ID recebido pela URL.
             */
            @PathVariable Long id,

            /*
             * Dados enviados no corpo da requisição.
             */
            @Valid
            @RequestBody
            BarbeiroRequestDTO dto) {

        /*
         * Envia o ID e os novos dados para o Service.
         */
        BarbeiroResponseDTO barbeiroAtualizado =
                barbeiroService.atualizar(id, dto);

        /*
         * Retorna HTTP 200 OK com os dados atualizados.
         */
        return ResponseEntity.ok(barbeiroAtualizado);
    }

    /*
     * PATCH /barbeiros/{id}/ativar
     *
     * Ativa um barbeiro.
     *
     * Exemplo:
     *
     * PATCH /barbeiros/2/ativar
     */
    @PatchMapping("/{id}/ativar")
    public ResponseEntity<BarbeiroResponseDTO> ativar(
            @PathVariable Long id) {

        /*
         * Chama o metodo ativar() do Service.
         */
        BarbeiroResponseDTO barbeiro =
                barbeiroService.ativar(id);

        /*
         * Retorna o barbeiro já atualizado.
         */
        return ResponseEntity.ok(barbeiro);
    }

    /*
     * PATCH /barbeiros/{id}/desativar
     *
     * Desativa um barbeiro.
     *
     * Exemplo:
     *
     * PATCH /barbeiros/2/desativar
     */
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<BarbeiroResponseDTO> desativar(
            @PathVariable Long id) {

        /*
         * Chama o metodo desativar() do Service.
         */
        BarbeiroResponseDTO barbeiro =
                barbeiroService.desativar(id);

        /*
         * Retorna HTTP 200 OK.
         */
        return ResponseEntity.ok(barbeiro);
    }
}