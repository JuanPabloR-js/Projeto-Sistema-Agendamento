package br.unisales.sistema_agendamento.usuario.service;

import br.unisales.sistema_agendamento.exception.ResourceNotFoundException;
import br.unisales.sistema_agendamento.usuario.dto.AtualizarUsuarioRequestDTO;
import br.unisales.sistema_agendamento.usuario.dto.TrocarSenhaRequestDTO;
import br.unisales.sistema_agendamento.usuario.dto.UsuarioResponseDTO;
import br.unisales.sistema_agendamento.usuario.model.Usuario;
import br.unisales.sistema_agendamento.usuario.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Camada de serviço responsável pelas regras relacionadas ao usuário.
 *
 * O Controller chama esta classe, e esta classe utiliza o Repository
 * para acessar os dados no banco.
 *
 * Fluxo principal:
 * Controller -> UsuarioService -> UsuarioRepository -> Banco
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    // Responsável por comparar e criptografar senhas.
    private final PasswordEncoder passwordEncoder;

    /**
     * As dependências são recebidas pelo construtor.
     * O Spring injeta automaticamente UsuarioRepository e PasswordEncoder.
     */
    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Altera a senha do usuário atualmente autenticado.
     *
     * 1. Descobre qual usuário está logado.
     * 2. Busca esse usuário pelo email.
     * 3. Confere se a senha atual informada está correta.
     * 4. Criptografa a nova senha.
     * 5. Salva a alteração no banco.
     */
    public void trocarSenha(TrocarSenhaRequestDTO dto) {

        // O Spring Security mantém os dados do usuário autenticado
        // no SecurityContext durante a requisição.
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        // Neste projeto, o nome da autenticação corresponde ao email.
        String email = authentication.getName();

        Usuario usuario = buscarEntidadePorEmail(email);

        // Compara a senha digitada com o hash armazenado no banco.
        // Nunca comparamos senha em texto puro diretamente com o valor salvo.
        boolean senhaCorreta = passwordEncoder.matches(
                dto.senhaAtual(),
                usuario.getSenha()
        );

        if (!senhaCorreta) {
            // TODO: futuramente vamos substituir por uma exception padronizada do projeto.
            throw new IllegalArgumentException("Senha atual incorreta");
        }

        // A nova senha também precisa ser criptografada antes de ser persistida.
        String novaSenhaCriptografada =
                passwordEncoder.encode(dto.novaSenha());

        usuario.setSenha(novaSenhaCriptografada);

        usuarioRepository.save(usuario);
    }

    /**
     * Busca um Usuario pelo seu ID.
     *
     * Este método pode ser reutilizado por outros módulos,
     * como Cliente e Barbeiro, que precisam da entidade Usuario completa.
     */
    public Usuario buscarEntidadePorId(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário não encontrado"));
    }

    /**
     * Busca um Usuario pelo email.
     *
     * Além deste módulo, pode ser utilizado pela camada de autenticação,
     * pois o email identifica o usuário no login.
     */
    public Usuario buscarEntidadePorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário não encontrado"));
    }

    /**
     * Converte a entidade Usuario em um DTO seguro para resposta da API.
     *
     * A entidade contém informações internas, como a senha,
     * que não devem ser enviadas ao front-end.
     */
    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTelefone(),
                usuario.getRole()
        );
    }

    /**
     * Retorna o perfil do usuário atualmente autenticado.
     *
     * O email é obtido através do Spring Security e usado
     * para localizar o Usuario correspondente no banco.
     */
    public UsuarioResponseDTO buscarMeuPerfil() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        Usuario usuario = buscarEntidadePorEmail(email);

        // Retorna um DTO em vez da entidade completa para não expor dados sensíveis.
        return toResponseDTO(usuario);
    }

    /**
     * Atualiza somente os dados permitidos do perfil do usuário autenticado.
     *
     * Atualmente são permitidas alterações em:
     * - nome
     * - telefone
     *
     * Email, senha e role possuem fluxos/regras separados.
     */
    public UsuarioResponseDTO atualizarMeuPerfil(
            AtualizarUsuarioRequestDTO dto) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        Usuario usuario = buscarEntidadePorEmail(email);

        usuario.setNome(dto.nome());
        usuario.setTelefone(dto.telefone());

        // Persiste as alterações no banco.
        usuarioRepository.save(usuario);

        // Devolve para a API somente os dados seguros do usuário atualizado.
        return toResponseDTO(usuario);
    }

}