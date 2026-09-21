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
 * @apiNote Serviço responsável pela criação e validação dos tokens JWT.
 * @author Juan Pablo Rocha Hempel
 * @since 11.09.2026
 */
@Service
public class JwtService {

    // Chave utilizada para assinar e validar os tokens.
    private final SecretKey chave;

    // Tempo de duração do token, em milissegundos.
    private final long tempoExpiracao;

    public JwtService(
            @Value("${jwt.secret}") String segredo,
            @Value("${jwt.expiration}") long tempoExpiracao
    ) {
        /*
         * A chave está configurada em Base64 no application.properties.
         * Primeiro decodificamos o texto e depois criamos a chave segura.
         */
        byte[] chaveDecodificada = Decoders.BASE64.decode(segredo);
        this.chave = Keys.hmacShaKeyFor(chaveDecodificada);
        this.tempoExpiracao = tempoExpiracao;
    }

    /*
     * Gera um token após o usuário informar e-mail e senha corretos.
     */
    public String gerarToken(UserDetails usuario) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + tempoExpiracao);

        return Jwts.builder()
                .subject(usuario.getUsername()) // O subject identifica o dono do token.
                .issuedAt(agora)// momento em q foi criado
                .expiration(expiracao)// prazo de validade do token
                .signWith(chave)// assina o token
                .compact(); //transforma em String
    }

    public String extrairEmail(String token) { // metodo para extrair email do armazenamento
        return extrairClaims(token).getSubject();
    }

    /*
     * O token será válido quando:
     * 1. Pertencer ao usuário carregado;
     * 2. Ainda não estiver expirado;
     * 3. Possuir uma assinatura válida.
     * A assinatura é verificada quando extrairClaims é executado.
     */
    public boolean tokenValido(String token, UserDetails usuario) {
        String email = extrairEmail(token);

        return email.equals(usuario.getUsername())
                && !tokenExpirado(token);
    }

    private boolean tokenExpirado(String token) { // metodo para validar se o token expirou
        return extrairClaims(token)
                .getExpiration()
                .before(new Date());
    }

    /*
     * Lê as informações internas do token.
     * verifyWith também confirma que o token foi assinado
     * utilizando a chave correta.
     */
    private Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}