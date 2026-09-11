package br.unisales.sistema_agendamento.auth.service;

import br.unisales.sistema_agendamento.exception.ResourceNotFoundException;
import br.unisales.sistema_agendamento.usuario.model.Usuario;
import br.unisales.sistema_agendamento.usuario.service.UsuarioService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @apiNote Serviço responsável por fornecer os dados do usuário ao Spring Security.
 * @author Juan Pablo Rocha Hempel
 * @since 11.09.2026
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioService usuarioService;

    public CustomUserDetailsService(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /*
     * Este metodo é chamado automaticamente pelo Spring Security
     * durante o login e durante a validação do token JWT.
     *
     * Para o Spring Security, o e-mail será utilizado como username.
     */
    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        try {
            Usuario usuario = usuarioService.buscarEntidadePorEmail(email);


            /*
             * Converte nosso Usuario em um UserDetails.
             *
             * username: informação usada para identificar o usuário.
             * password: senha criptografada que está salva no banco.
             * roles: perfil usado para verificar permissões.
             * disabled: impede o login quando o usuário está inativo.
             */
            return User.builder()
                    .username(usuario.getEmail())
                    .password(usuario.getSenha())
                    .roles(usuario.getRole().name())
                    .disabled(!usuario.isAtivo())
                    .build();

        } catch (ResourceNotFoundException exception) {
            /*
             * O Spring Security espera uma UsernameNotFoundException
             * quando o usuário não for encontrado.
             */
            throw new UsernameNotFoundException(
                    "Usuário não encontrado com o e-mail informado",
                    exception
            );
        }
    }
}