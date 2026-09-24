package com.levir.hernandez.producto.infrastructure.adapter.in.web.exception;

import com.levir.hernandez.producto.application.exception.RecursoNoEncontradoException;
import com.levir.hernandez.producto.application.exception.ServicioNoDisponibleException;
import com.levir.hernandez.producto.domain.exception.DomainException;
import com.levir.hernandez.producto.infrastructure.adapter.in.web.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import java.util.List;

/**
 * Conversor global de excepciones a respuestas y codigos de error HTTP
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler
{
    // Recurso inexistente (propio o de otro servicio)
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException ex, ServerWebExchange exchange)
    {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), exchange, List.of());
    }

    // Otro microservicio necesario para la operacion no respondio o su circuito esta abierto
    @ExceptionHandler(ServicioNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> servicioNoDisponible(ServicioNoDisponibleException ex,
                                                              ServerWebExchange exchange)
    {
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), exchange, List.of());
    }

    // Regla de negocio incumplida (nombre vacio, stock negativo...)
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> reglaDeDominio(DomainException ex, ServerWebExchange exchange)
    {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), exchange, List.of());
    }

    // Campos del cuerpo que no pasan @Valid
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponse> validacion(WebExchangeBindException ex, ServerWebExchange exchange)
    {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return build(HttpStatus.BAD_REQUEST, "La peticion contiene campos invalidos", exchange, detalles);
    }

    // Cuerpo JSON ilegible o identificador que no es un UUID
    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ErrorResponse> peticionIlegible(ServerWebInputException ex, ServerWebExchange exchange)
    {
        return build(HttpStatus.BAD_REQUEST, "La peticion tiene un formato invalido", exchange, List.of());
    }

    // Maneja errores no controlados y errores HTTP generados por Spring WebFlux
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> inesperado(Exception ex, ServerWebExchange exchange)
    {
        if (ex instanceof org.springframework.web.ErrorResponse errorWeb)
        {
            HttpStatus status = HttpStatus.valueOf(errorWeb.getStatusCode().value());
            return build(status, status.getReasonPhrase(), exchange, List.of());
        }

        log.error("Error no controlado en {}", ruta(exchange), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Se ha producido un error inesperado", exchange, List.of());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String mensaje, ServerWebExchange exchange,
                                                List<String> detalles)
    {
        return ResponseEntity.status(status).body(ErrorResponse.of(
                status.value(), status.getReasonPhrase(), mensaje, ruta(exchange), detalles));
    }

    private static String ruta(ServerWebExchange exchange)
    {
        return exchange.getRequest().getPath().value();
    }
}
