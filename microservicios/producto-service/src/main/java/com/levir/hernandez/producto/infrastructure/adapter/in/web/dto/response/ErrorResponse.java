package com.levir.hernandez.producto.infrastructure.adapter.in.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Formato estandar para las respuestas de error de la API
 * **/
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String mensaje,
        String path,
        List<String> detalles)
{
    public static ErrorResponse of(int status, String error, String mensaje, String path, List<String> detalles)
    {
        return new ErrorResponse(Instant.now(), status, error, mensaje, path, detalles);
    }
}
