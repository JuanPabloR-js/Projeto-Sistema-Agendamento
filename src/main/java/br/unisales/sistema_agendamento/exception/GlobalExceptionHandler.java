package br.unisales.sistema_agendamento.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @apiNote Esta classe captura as exceptions e transforma os erros em respostas JSON padronizadas.
 * @author Juan Pablo Rocha Hempel
 * @since 02.09.2026
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> tratarRecursoNaoEncontrado(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problema = criarProblema(
                HttpStatus.NOT_FOUND,
                "Recurso não encontrado",
                exception.getMessage(),
                request
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(problema);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> tratarRegraDeNegocio(
            BusinessException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problema = criarProblema(
                HttpStatus.BAD_REQUEST,
                "Regra de negócio inválida",
                exception.getMessage(),
                request
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(problema);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ProblemDetail> tratarConflito(
            ConflictException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problema = criarProblema(
                HttpStatus.CONFLICT,
                "Conflito de dados",
                exception.getMessage(),
                request
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(problema);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> tratarErroDeValidacao(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> erros = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(erro -> erros.put(
                        erro.getField(),
                        erro.getDefaultMessage()
                ));

        ProblemDetail problema = criarProblema(
                HttpStatus.BAD_REQUEST,
                "Erro de validação",
                "Um ou mais campos estão inválidos.",
                request
        );

        problema.setProperty("erros", erros);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(problema);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> tratarJsonInvalido(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problema = criarProblema(
                HttpStatus.BAD_REQUEST,
                "Requisição inválida",
                "O corpo da requisição possui informações inválidas.",
                request
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(problema);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> tratarAcessoNegado(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problema = criarProblema(
                HttpStatus.FORBIDDEN,
                "Acesso negado",
                "Você não possui permissão para realizar esta operação.",
                request
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(problema);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> tratarErroInterno(
            Exception exception,
            HttpServletRequest request
    ) {
        logger.error("Erro interno não tratado", exception);

        ProblemDetail problema = criarProblema(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno",
                "Ocorreu um erro interno inesperado.",
                request
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(problema);
    }

    private ProblemDetail criarProblema(
            HttpStatus status,
            String titulo,
            String mensagem,
            HttpServletRequest request
    ) {
        ProblemDetail problema =
                ProblemDetail.forStatusAndDetail(status, mensagem);

        problema.setTitle(titulo);
        problema.setProperty("timestamp", LocalDateTime.now());
        problema.setProperty("path", request.getRequestURI());

        return problema;
    }
}