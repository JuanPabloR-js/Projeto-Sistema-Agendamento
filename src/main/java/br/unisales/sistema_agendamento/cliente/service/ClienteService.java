// Feito por: moacyr dev2

package br.unisales.sistema_agendamento.cliente.service;

import org.springframework.transaction.annotation.Transactional;
import br.unisales.sistema_agendamento.cliente.domain.Cliente;
import br.unisales.sistema_agendamento.cliente.dto.ClienteRequestDTO;
import br.unisales.sistema_agendamento.cliente.dto.ClienteResponseDTO;
import br.unisales.sistema_agendamento.cliente.repository.ClienteRepository;
import br.unisales.sistema_agendamento.exception.BusinessException;
import br.unisales.sistema_agendamento.exception.ConflictException;
import br.unisales.sistema_agendamento.exception.ResourceNotFoundException;
import br.unisales.sistema_agendamento.usuario.model.Role;
import br.unisales.sistema_agendamento.usuario.model.Usuario;
import br.unisales.sistema_agendamento.usuario.service.UsuarioService;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioService usuarioService;

    public ClienteService(
            ClienteRepository clienteRepository,
            UsuarioService usuarioService
    ) {
        this.clienteRepository = clienteRepository;
        this.usuarioService = usuarioService;
    }

    @Transactional
    public ClienteResponseDTO criar(ClienteRequestDTO dto) {
        if (clienteRepository.existsByUsuarioId(dto.getUsuarioId())) {
            throw new ConflictException(
                    "Este usuário já possui um perfil de cliente."
            );
        }

        Usuario usuario = usuarioService.buscarEntidadePorId(dto.getUsuarioId());
        validarRoleCliente(usuario);

        Cliente cliente = Cliente.builder()
                .usuario(usuario)
                .telefone(dto.getTelefone())
                .build();

        return converterParaResponseDTO(clienteRepository.save(cliente));
    }

    public ClienteResponseDTO buscarPorId(Long clienteId) {
        return converterParaResponseDTO(buscarEntidadePorId(clienteId));
    }

    public ClienteResponseDTO buscarPorUsuarioId(Long usuarioId) {
        Cliente cliente = clienteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente não encontrado para o usuário informado."
                ));

        return converterParaResponseDTO(cliente);
    }

    @Transactional
    public ClienteResponseDTO atualizar(
            Long clienteId,
            ClienteRequestDTO dto
    ) {
        Cliente cliente = buscarEntidadePorId(clienteId);
        if (!Objects.equals(
                cliente.getUsuario().getId(),
                dto.getUsuarioId()
        )) {
            throw new BusinessException(
                    "Não é permitido alterar o usuário vinculado ao cliente."
            );
        }

        Usuario usuario = usuarioService.buscarEntidadePorId(dto.getUsuarioId());

        validarRoleCliente(usuario);

        Long usuarioAtualId = cliente.getUsuario().getId();
        Long novoUsuarioId = usuario.getId();

        // Só verifica duplicidade quando o usuário relacionado for alterado.
        if (!Objects.equals(usuarioAtualId, novoUsuarioId)
                && clienteRepository.existsByUsuarioId(novoUsuarioId)) {
            throw new ConflictException(
                    "Este usuário já possui um perfil de cliente."
            );
        }

        cliente.setUsuario(usuario);
        cliente.setTelefone(dto.getTelefone());

        return converterParaResponseDTO(clienteRepository.save(cliente));
    }

    // Usado por outros módulos, como o de Agendamento.
    public Cliente buscarEntidadePorId(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente não encontrado."
                ));
    }

    private void validarRoleCliente(Usuario usuario) {
        if (usuario.getRole() != Role.CLIENTE) {
            throw new BusinessException(
                    "Apenas usuários com role CLIENTE podem possuir perfil de cliente."
            );
        }
    }

    private ClienteResponseDTO converterParaResponseDTO(Cliente cliente) {
        return ClienteResponseDTO.builder()
                .id(cliente.getId())
                .usuarioId(cliente.getUsuario().getId())
                .nome(cliente.getUsuario().getNome())
                .telefone(cliente.getTelefone())
                .build();
    }
}
