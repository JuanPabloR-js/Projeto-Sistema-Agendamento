package br.unisales.sistema_agendamento.servico.exception;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail entityNotFound(EntityNotFoundException e, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setTitle(e.getMessage());
        problemDetail.setStatus(404);
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail methodArgumentInvalid(MethodArgumentNotValidException e,
            HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setTitle("Dados inválidos");
        problemDetail.setStatus(422);
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        List<Map<String, String>> errors = e.getBindingResult().getFieldErrors().stream()
                .map(x -> Map.of("field", x.getField(), "message", x.getDefaultMessage())).toList();

        problemDetail.setProperty("errors", errors);
        return problemDetail;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail dataIntegrityViolation(DataIntegrityViolationException e,
            HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setTitle(e.getMessage());
        problemDetail.setStatus(400);
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        return problemDetail;
    }
}