package com.levir.hernandez.producto.infrastructure.adapter.in.web.dto.response;

import org.springframework.hateoas.server.core.Relation;

import java.util.UUID;

/** Producto con mas stock de una sucursal, indicando a que sucursal pertenece. */
@Relation(itemRelation = "producto", collectionRelation = "productos")
public record ProductoConMayorStockResponse(
        UUID sucursalId,
        String sucursalNombre,
        UUID productoId,
        String productoNombre,
        Integer stock)
{
}
