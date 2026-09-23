package com.levir.hernandez.sucursal.infrastructure.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo comun para crear franquicias y sucursales, y para renombrar cualquier recurso. */
public record NombreRecursoRequest(
        @Schema(example = "Burger Express")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre)
{
}
