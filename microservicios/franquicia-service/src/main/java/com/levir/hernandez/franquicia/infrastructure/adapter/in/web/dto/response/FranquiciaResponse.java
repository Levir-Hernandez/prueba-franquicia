package com.levir.hernandez.franquicia.infrastructure.adapter.in.web.dto.response;

import org.springframework.hateoas.server.core.Relation;

import java.util.UUID;

@Relation(itemRelation = "franquicia", collectionRelation = "franquicias")
public record FranquiciaResponse(UUID id, String nombre)
{
}
