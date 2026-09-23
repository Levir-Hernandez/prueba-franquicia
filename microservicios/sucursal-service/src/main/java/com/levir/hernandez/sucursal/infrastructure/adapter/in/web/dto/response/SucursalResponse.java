package com.levir.hernandez.sucursal.infrastructure.adapter.in.web.dto.response;

import org.springframework.hateoas.server.core.Relation;

import java.util.UUID;

@Relation(itemRelation = "sucursal", collectionRelation = "sucursales")
public record SucursalResponse(UUID id, String nombre, UUID franquiciaId)
{
}
