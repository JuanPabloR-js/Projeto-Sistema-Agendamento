package br.unisales.sistema_agendamento.exception;

/**
 * @apiNote Esta classe é usada quando alguma informação não for encontrada no banco.
 * @author Juan Pablo Rocha Hempel
 * @since 02.09.2026
 */

public class ResourceNotFoundException extends RuntimeException{

    //404 Not Found
    public ResourceNotFoundException(String mensagem) {
        super(mensagem);
    }
}
