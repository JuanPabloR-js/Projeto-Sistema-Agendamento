package br.unisales.sistema_agendamento.auth.service;

import br.unisales.sistema_agendamento.auth.dto.CadastroClienteRequestDTO;
import br.unisales.sistema_agendamento.cliente.dto.ClienteRequestDTO;
import br.unisales.sistema_agendamento.cliente.dto.ClienteResponseDTO;
import br.unisales.sistema_agendamento.cliente.service.ClienteService;
import br.unisales.sistema_agendamento.usuario.model.Role;
import org.springframework.transaction.annotation.Transactional;
import br.unisales.sistema_agendamento.auth.dto.LoginRequestDTO;
import br.unisales.sistema_agendamento.auth.dto.LoginResponseDTO;
import br.unisales.sistema_agendamento.security.JwtService;
import br.unisales.sistema_agendamento.usuario.model.Usuario;
import br.unisales.sistema_agendamento.usuario.service.UsuarioService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * @apiNote Serviço responsável pela autenticação dos usuários.
 * @author Juan Pablo Rocha Hempel
 * @since 11.09.2026
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final ClienteService clienteService;
    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            CustomUserDetailsService userDetailsService,
            ClienteService clienteService,
            UsuarioService usuarioService,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.clienteService = clienteService;
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
    }

    /*
     * Envia o e-mail e a senha para o Spring Security.
     *
     * Internamente ele:
     * 1. Chama o CustomUserDetailsService;
     * 2. Busca o usuário pelo e-mail;
     * 3. Compara a senha informada com a senha criptografada;
     * 4. Lança uma AuthenticationException se estiver incorreta.
     */
    public LoginResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.senha()
                )
        );

        /*
         * Se chegou aqui, significa que o e-mail e a senha
         * foram validados corretamente.
         */
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(request.email());

        // Busca a entidade para devolver os dados do usuário na resposta.
        Usuario usuario =
                usuarioService.buscarEntidadePorEmail(request.email());

        // Gera o JWT que será utilizado nas próximas requisições.
        String token = jwtService.gerarToken(userDetails);

        return new LoginResponseDTO(
                token,
                "Bearer",
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole().name()
        );
    }

    @Transactional
    public ClienteResponseDTO cadastrar(CadastroClienteRequestDTO dto) {
        /*
         * A role é definida pelo backend.
         * O cadastro público não pode escolher ADMIN ou BARBEIRO.
         */
        Usuario usuario = usuarioService.criar(
                dto.nome(),
                dto.email(),
                dto.telefone(),
                dto.senha(),
                Role.CLIENTE
        );

        // Vincula o novo perfil à conta que acabou de ser criada.
        ClienteRequestDTO clienteDTO = new ClienteRequestDTO(
                usuario.getId(),
                dto.telefone()
        );

        return clienteService.criar(clienteDTO);
    }
}