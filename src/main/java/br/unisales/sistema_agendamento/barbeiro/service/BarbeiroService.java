package br.unisales.sistema_agendamento.barbeiro.service;

import br.unisales.sistema_agendamento.barbeiro.dto.BarbeiroRequestDTO;
import br.unisales.sistema_agendamento.barbeiro.dto.BarbeiroResponseDTO;
import br.unisales.sistema_agendamento.barbeiro.model.Barbeiro;
import br.unisales.sistema_agendamento.barbeiro.repository.BarbeiroRepository;
import br.unisales.sistema_agendamento.exception.BusinessException;
import br.unisales.sistema_agendamento.exception.ConflictException;
import br.unisales.sistema_agendamento.exception.ResourceNotFoundException;
import br.unisales.sistema_agendamento.usuario.model.Role;
import br.unisales.sistema_agendamento.usuario.model.Usuario;
import br.unisales.sistema_agendamento.usuario.service.UsuarioService;
import org.springframework.stereotype.Service;

import java.util.List;

// Indica ao Spring que esta classe contém regras de negócio.
// O Spring passa a gerenciar essa classe e pode injetá-la em outros lugares.
@Service
public class BarbeiroService {

    // Repository responsável por acessar os dados de Barbeiro no banco.
    private final BarbeiroRepository barbeiroRepository;

    // Service de Usuario.
    // Usamos ele para buscar o Usuario relacionado ao Barbeiro.
    private final UsuarioService usuarioService;

    // Construtor usado pelo Spring para injetar as dependências.
    public BarbeiroService(
            BarbeiroRepository barbeiroRepository,
            UsuarioService usuarioService) {

        this.barbeiroRepository = barbeiroRepository;
        this.usuarioService = usuarioService;
    }

    // Cria um novo perfil de barbeiro.
    public BarbeiroResponseDTO criar(BarbeiroRequestDTO dto) {

        // Busca o Usuario pelo ID recebido no DTO.
        // Não acessamos UsuarioRepository diretamente.
        Usuario usuario =
                usuarioService.buscarEntidadePorId(dto.getUsuarioId());

        // Regra de negócio:
        // somente Usuario com role BARBEIRO pode ter perfil de barbeiro.
        if (usuario.getRole() != Role.BARBEIRO) {
            throw new BusinessException(
                    "Apenas usuários com role BARBEIRO podem possuir perfil de barbeiro."
            );
        }

        // Verifica se já existe um barbeiro associado ao mesmo Usuario.
        //
        // findByUsuarioId(...) retorna Optional<Barbeiro>.
        // isPresent() retorna true caso exista um barbeiro encontrado.
        if (barbeiroRepository
                .findByUsuarioId(usuario.getId())
                .isPresent()) {

            throw new ConflictException(
                    "Este usuário já possui um perfil de barbeiro."
            );
        }

        // Cria um novo objeto Barbeiro.
        //
        // usuario -> Usuario encontrado anteriormente
        // especialidade -> enviada no DTO
        // true -> barbeiro comeca ativo
        Barbeiro barbeiro = new Barbeiro(
                usuario,
                dto.getEspecialidade(),
                true
        );

        // Salva o objeto no banco de dados.
        //
        // O metodo save() é fornecido pelo JpaRepository.
        Barbeiro barbeiroSalvo =
                barbeiroRepository.save(barbeiro);

        // Converte a entidade Barbeiro para um DTO seguro
        // antes de devolver para a API.
        return converterParaResponseDTO(barbeiroSalvo);
    }

    // Busca um barbeiro pelo ID e retorna os dados em formato de DTO.
    public BarbeiroResponseDTO buscarPorId(Long barbeiroId) {

        // Reutilizamos buscarEntidadePorId() para evitar repetir código.
        Barbeiro barbeiro =
                buscarEntidadePorId(barbeiroId);

        // Converte a entidade encontrada para DTO.
        return converterParaResponseDTO(barbeiro);
    }

    // Lista todos os barbeiros cadastrados.
    public List<BarbeiroResponseDTO> listarTodos() {

        // findAll() busca todos os Barbeiros no banco.
        //
        // stream() permite trabalhar com cada elemento da lista.
        //
        // map(...) transforma cada Barbeiro em BarbeiroResponseDTO.
        //
        // toList() cria a lista final de DTOs.
        return barbeiroRepository.findAll()
                .stream()
                .map(this::converterParaResponseDTO)
                .toList();
    }

    // Atualiza os dados de um barbeiro já existente.
    public BarbeiroResponseDTO atualizar(
            Long barbeiroId,
            BarbeiroRequestDTO dto) {

        // Primeiro verificamos se o barbeiro realmente existe.
        Barbeiro barbeiro =
                buscarEntidadePorId(barbeiroId);

        // Busca o Usuario informado na atualização.
        Usuario usuario =
                usuarioService.buscarEntidadePorId(dto.getUsuarioId());

        // O Usuario precisa continuar tendo role BARBEIRO.
        if (usuario.getRole() != Role.BARBEIRO) {
            throw new BusinessException(
                    "Apenas usuários com role BARBEIRO podem possuir perfil de barbeiro."
            );
        }

        /*
         * Verifica se o Usuario informado já pertence a OUTRO barbeiro.
         *
         * Exemplo:
         *
         * Barbeiro 1 -> Usuario 10
         * Barbeiro 2 -> Usuario 20
         *
         * Se tentarmos atualizar o Barbeiro 2 usando Usuario 10,
         * devemos impedir, porque Usuario 10 já pertence ao Barbeiro 1.
         */
        barbeiroRepository.findByUsuarioId(usuario.getId())

                // Se encontrou um barbeiro com esse Usuario,
                // verificamos se ele é diferente do barbeiro que estamos atualizando.
                .filter(outroBarbeiro ->
                        !outroBarbeiro.getId().equals(barbeiroId))

                // Se ainda existir um resultado depois do filtro,
                // significa que o Usuario pertence a outro Barbeiro.
                .ifPresent(outroBarbeiro -> {
                    throw new ConflictException(
                            "Este usuário já possui um perfil de barbeiro."
                    );
                });

        // Atualiza o Usuario associado ao barbeiro.
        barbeiro.setUsuario(usuario);

        // Atualiza a especialidade.
        barbeiro.setEspecialidade(dto.getEspecialidade());

        // Salva as alterações no banco.
        Barbeiro barbeiroAtualizado =
                barbeiroRepository.save(barbeiro);

        // Retorna os dados atualizados em formato de DTO.
        return converterParaResponseDTO(barbeiroAtualizado);
    }

    // Ativa um barbeiro.
    public BarbeiroResponseDTO ativar(Long barbeiroId) {

        // Busca o barbeiro ou lança erro se não existir.
        Barbeiro barbeiro =
                buscarEntidadePorId(barbeiroId);

        // Define o status como ativo.
        barbeiro.setAtivo(true);

        // Salva a alteração.
        Barbeiro barbeiroAtualizado =
                barbeiroRepository.save(barbeiro);

        // Retorna os dados atualizados.
        return converterParaResponseDTO(barbeiroAtualizado);
    }

    // Desativa um barbeiro.
    public BarbeiroResponseDTO desativar(Long barbeiroId) {

        // Busca o barbeiro ou lança erro caso não exista.
        Barbeiro barbeiro =
                buscarEntidadePorId(barbeiroId);

        // Define o status como inativo.
        barbeiro.setAtivo(false);

        // Salva a alteração no banco.
        Barbeiro barbeiroAtualizado =
                barbeiroRepository.save(barbeiro);

        // Retorna o Barbeiro atualizado em formato de DTO.
        return converterParaResponseDTO(barbeiroAtualizado);
    }

    /*
     * Busca a entidade Barbeiro completa pelo ID.
     *
     * Este metodo é importante porque outros módulos,
     * como Agendamento, precisarão receber o objeto Barbeiro completo.
     */
    public Barbeiro buscarEntidadePorId(Long barbeiroId) {

        // findById retorna Optional<Barbeiro>.
        //
        // Se encontrar, retorna o Barbeiro.
        // Se não encontrar, lança ResourceNotFoundException.
        return barbeiroRepository.findById(barbeiroId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Barbeiro não encontrado."
                        )
                );
    }

    /*
     * Converte uma entidade Barbeiro em BarbeiroResponseDTO.
     *
     * Fazemos isso para não devolver a entidade completa diretamente na API.
     * Assim conseguimos controlar exatamente quais informações serão enviadas.
     */
    private BarbeiroResponseDTO converterParaResponseDTO(
            Barbeiro barbeiro) {

        return new BarbeiroResponseDTO(

                // ID do barbeiro.
                barbeiro.getId(),

                // ID do Usuario relacionado.
                barbeiro.getUsuario().getId(),

                // Nome do Usuario relacionado.
                barbeiro.getUsuario().getNome(),

                // Especialidade do barbeiro.
                barbeiro.getEspecialidade(),

                // Status ativo/inativo.
                barbeiro.getAtivo()
        );
    }
}