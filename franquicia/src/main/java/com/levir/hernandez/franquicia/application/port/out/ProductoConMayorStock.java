package com.levir.hernandez.franquicia.application.port.out;

import java.util.UUID;

/**
 * Proyeccion de lectura: el producto con mas stock de una sucursal, indicando a que sucursal pertenece.
 */
public record ProductoConMayorStock(
        UUID productoId,
        String productoNombre,
        Integer stock,
        UUID sucursalId,
        String sucursalNombre)
{
}
