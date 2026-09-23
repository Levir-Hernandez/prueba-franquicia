package com.levir.hernandez.producto.application.port.out;

import java.util.UUID;

/**
 * Datos minimos de una sucursal que producto-service obtiene de sucursal-service
 */
public record SucursalResumen(UUID id, String nombre)
{
}
