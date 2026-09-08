package br.unisales.sistema_agendamento.exception;

/**
 * @apiNote Esta classe é utilizada quando existe conflito com alguma informação já cadastrada.
 * @author Juan Pablo Rocha Hempel
 * @since 02.09.2026
 */

public class ConflictException extends RuntimeException{

    //409 Conflict
    public ConflictException(String mensagem) {
        super(mensagem);
    }
}
