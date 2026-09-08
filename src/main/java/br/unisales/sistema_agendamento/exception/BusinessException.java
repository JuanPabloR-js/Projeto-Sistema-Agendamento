package br.unisales.sistema_agendamento.exception;

/**
 * @apiNote Esta classe é utilizada quando uma regra geral de negócio não for respeitada.
 * @author Juan Pablo Rocha Hempel
 * @since 02.09.2026
 */

public class BusinessException extends RuntimeException{

    //400 Bad Request
    public BusinessException(String mensagem) {
        super(mensagem);
    }
}
