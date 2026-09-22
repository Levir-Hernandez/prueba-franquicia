package com.levir.hernandez.franquicia.infrastructure.adapter.in.web.dto.response;

import org.springframework.hateoas.server.core.Relation;

import java.util.UUID;

@Relation(itemRelation = "producto", collectionRelation = "productos")
public record ProductoResponse(UUID id, String nombre, Integer stock, UUID sucursalId)
{
}
