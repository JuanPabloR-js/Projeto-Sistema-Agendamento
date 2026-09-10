package br.unisales.sistema_agendamento.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * @apiNote Classe responsável pela geração e validação dos tokens JWT.
 * @author Juan Pablo Rocha Hempel
 * @since 02.09.2026
 */
@Service
public class JwtService {

    private final String chaveSecreta;
    private final long tempoExpiracao;

    public JwtService( //construtor
            @Value("${jwt.secret}") String chaveSecreta, // linha que busca o secret colocado no application.properties
            @Value("${jwt.expiration}") long tempoExpiracao // linha que busca o tempo de expiração do token
    ) {
        this.chaveSecreta = chaveSecreta;
        this.tempoExpiracao = tempoExpiracao;
    }

    public String gerarToken(UserDetails usuario) { // metodo para gerar o token, pegando o usuário autenticado
        Date dataCriacao = new Date();

        Date dataExpiracao = new Date(
                dataCriacao.getTime() + tempoExpiracao
        );

        return Jwts.builder()
                .subject(usuario.getUsername()) // guarda o email dentro do token
                .issuedAt(dataCriacao) // registra quando o token foi gerado
                .expiration(dataExpiracao) // registra quando o token deixa de funcionar
                .signWith(obterChaveAssinatura()) // assina o token, impede que alguém o modifique
                .compact(); // transforma tudo em uma String
    }

    public String extrairEmail(String token) { // metodo onde abre o token e recupera o email salvo no subject
        return extrairClaims(token).getSubject();
    }

    public boolean tokenValido( // verificação de token
            String token,
            UserDetails usuario
    ) {
        String emailDoToken = extrairEmail(token);

        return emailDoToken.equals(usuario.getUsername()) // verifica se o token pertence ao usuário encontrado
                && usuario.isEnabled() // verifica se usuário está ativo
                && !tokenExpirado(token); // verifica se o token ainda está no prazo
    }

    private boolean tokenExpirado(String token) { // metodo para verificar se o token ainda esta no prazo
        Date dataExpiracao = extrairClaims(token).getExpiration();

        return dataExpiracao.before(new Date());
    }

    private Claims extrairClaims(String token) { // os Claims são os dados e informações armazenadas dentro do token
        return Jwts.parser()
                .verifyWith(obterChaveAssinatura())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey obterChaveAssinatura() {
        byte[] chaveDecodificada =
                Decoders.BASE64.decode(chaveSecreta);

        return Keys.hmacShaKeyFor(chaveDecodificada);
    }
}