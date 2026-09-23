package com.levir.hernandez.franquicia.infrastructure.adapter.in.web.exception;

import com.levir.hernandez.franquicia.application.exception.RecursoNoEncontradoException;
import com.levir.hernandez.franquicia.domain.exception.DomainException;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

/**
 * Conversor global de excepciones a respuestas y codigos de error HTTP
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler
{
    // Franquicia inexistente
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest request)
    {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
    }

    // Regla de negocio incumplida (nombre vacio, stock negativo...)
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> reglaDeDominio(DomainException ex, HttpServletRequest request)
    {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, List.of());
    }

    // Campos del cuerpo que no pasan @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex, HttpServletRequest request)
    {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return build(HttpStatus.BAD_REQUEST, "La peticion contiene campos invalidos", request, detalles);
    }

    // Cuerpo JSON ilegible o identificador que no es un UUID
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> peticionIlegible(Exception ex, HttpServletRequest request)
    {
        return build(HttpStatus.BAD_REQUEST, "La peticion tiene un formato invalido", request, List.of());
    }

    // Maneja errores no controlados y errores HTTP generados por Spring MVC
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> inesperado(Exception ex, HttpServletRequest request)
    {
        if (ex instanceof org.springframework.web.ErrorResponse errorWeb)
        {
            HttpStatus status = HttpStatus.valueOf(errorWeb.getStatusCode().value());
            return build(status, status.getReasonPhrase(), request, List.of());
        }

        log.error("Error no controlado en {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Se ha producido un error inesperado", request, List.of());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String mensaje, HttpServletRequest request,
                                                List<String> detalles)
    {
        return ResponseEntity.status(status).body(ErrorResponse.of(
                status.value(), status.getReasonPhrase(), mensaje, request.getRequestURI(), detalles));
    }
}
