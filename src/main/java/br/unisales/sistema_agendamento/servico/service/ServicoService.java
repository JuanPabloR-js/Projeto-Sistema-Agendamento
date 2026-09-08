package br.unisales.sistema_agendamento.servico.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.unisales.sistema_agendamento.servico.dto.ServicoRequest;
import br.unisales.sistema_agendamento.servico.dto.ServicoResponse;
import br.unisales.sistema_agendamento.servico.entity.Servico;
import br.unisales.sistema_agendamento.servico.repository.ServicoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * @apiNote Serviço que lida com a lógica de negócio da entidade "Servico".
 * @author Mateus Alves Costa
 * @since 07.09.2026
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServicoService {
    private final ServicoRepository repository;

    /**
     * @apiNote Cria um novo serviço com base nos dados fornecidos.
     * @param request --> Dados para criação do serviço.
     * @return --> Retorna um objeto ServicoResponse contendo os dados do serviço
     *         criado.
     * @throws DataIntegrityViolationException --> Lançada caso o nome fornecido já
     *                                         esteja
     *                                         cadastrado.
     * @author Mateus Alves Costa
     * @since 07.09.2026
     */
    @Transactional(readOnly = false)
    public ServicoResponse criar(ServicoRequest request) {
        if (repository.existsByNome(request.nome())) {
            throw new DataIntegrityViolationException("Serviço com o mesmo nome já existe.");
        }
        Servico servico = new Servico(request.nome().trim(), request.descricao(),
                request.duracaoMinutos(), request.preco(), request.ativo());
        servico = repository.save(servico);
        return toDto(servico);
    }

    /**
     * @apiNote Busca um serviço pelo seu ID.
     * @param id --> ID do serviço a ser buscado.
     * @return --> Retorna um objeto ServicoResponse contendo os dados do serviço
     *         encontrado.
     * @throws EntityNotFoundException --> Lançada caso o serviço com o ID fornecido
     *                                 não seja
     *                                 encontrado.
     * @author Mateus Alves Costa
     * @since 07.09.2026
     */
    public ServicoResponse buscarPorId(Long id) {
        Servico servico = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Serviço não encontrado com o ID: " + id));
        return toDto(servico);
    }

    /**
     * @apiNote Busca serviços com base em critérios de filtro, como nome e status
     *          do serviço.
     * @param nome     --> Nome do serviço a ser filtrado.
     * @param ativo    --> Status do serviço a ser filtrado.
     * @param pageable --> Configuração de paginação.
     * @return --> Retorna uma página com os dados dos serviços encontrados.
     * @author Mateus Alves Costa
     * @since 07.09.2026
     */
    public Page<ServicoResponse> buscar(String nome, Boolean ativo, Pageable pageable) {
        Specification<Servico> spec = (root, query, cb) -> cb.conjunction();

        if (nome != null && !nome.isBlank())
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%"));

        if (ativo != null)
            spec = spec.and(
                    (root, query, cb) -> cb.equal(root.get("ativo"), ativo));
        return repository.findAll(spec, pageable).map(this::toDto);
    }

    /**
     * @apiNote Lista todos os serviços.
     * @param pageable --> Configuração de paginação.
     * @return --> Retorna uma página de objetos ServicoResponse.
     * @author Mateus Alves Costa
     * @since 07.09.2026
     */
    public Page<ServicoResponse> listar(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDto);
    }

    /**
     * @apiNote Atualiza um serviço existente com base nos dados fornecidos.
     * @param id      --> ID do serviço a ser atualizado.
     * @param request --> Dados para atualização do serviço.
     * @return --> Retorna um objeto ServicoResponse contendo os dados do serviço
     *         atualizado.
     * @throws DataIntegrityViolationException --> Lançada caso o nome fornecido já
     *                                         esteja
     *                                         cadastrado em outro serviço.
     * @throws EntityNotFoundException         --> Lançada caso o serviço com o ID
     *                                         fornecido não seja
     *                                         encontrado.
     * @author Mateus Alves Costa
     * @since 07.09.2026
     */
    @Transactional(readOnly = false)
    public ServicoResponse atualizar(Long id, ServicoRequest request) {
        if (repository.existsByNomeAndIdNot(request.nome().trim(), id)) {
            throw new DataIntegrityViolationException("Serviço com o mesmo nome já existe.");
        }

        Servico servico = repository.findById(id).orElseThrow(
            () -> new EntityNotFoundException("Serviço não encontrado com o ID: " + id)
        );

        if (request.nome() != null) servico.setNome(request.nome().trim());
        if (request.descricao() != null) servico.setDescricao(request.descricao());
        if (request.duracaoMinutos() != null) servico.setDuracaoMinutos(request.duracaoMinutos());
        if (request.preco() != null) servico.setPreco(request.preco());
        if (request.ativo() != null)servico.setAtivo(request.ativo());

        servico = repository.save(servico);
        return toDto(servico);
    }

    /**
     * @apiNote Ativa ou desativa um serviço com base no valor fornecido.
     * @param id    --> ID do serviço a ser ativado/desativado.
     * @param valor --> Valor booleano indicando se o serviço deve ser ativado
     *              (true) ou
     *              desativado (false).
     * @throws EntityNotFoundException --> Lançada caso o serviço com o ID fornecido
     *                                 não seja
     *                                 encontrado.
     * @author Mateus Alves Costa
     * @since 07.09.2026
     */
    @Transactional(readOnly = false)
    public void ativarDesativar(Long id, Boolean valor) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Serviço não encontrado com o ID: " + id);
        }
        Servico servico = repository.getReferenceById(id);
        servico.setAtivo(valor);
        repository.save(servico);
    }

    /**
     * @apiNote Converte uma entidade Servico em um objeto ServicoResponse.
     * @param servico --> Entidade Servico a ser convertida.
     * @return --> Retorna um objeto ServicoResponse contendo os dados do serviço.
     * @author Mateus Alves Costa
     * @since 07.09.2026
     */
    private ServicoResponse toDto(Servico servico) {
        return new ServicoResponse(servico.getId(), servico.getNome(), servico.getDescricao(),
                servico.getDuracaoMinutos(), servico.getPreco(), servico.getAtivo());
    }

}
