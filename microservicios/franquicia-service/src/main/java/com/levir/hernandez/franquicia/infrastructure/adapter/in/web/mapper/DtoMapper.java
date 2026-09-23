package com.levir.hernandez.franquicia.infrastructure.adapter.in.web.mapper;

import com.levir.hernandez.franquicia.domain.model.Franquicia;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.dto.response.FranquiciaResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Mapper encargado de convertir objetos de dominio en DTOs de respuesta
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DtoMapper
{
    public static FranquiciaResponse toResponse(Franquicia franquicia)
    {
        return new FranquiciaResponse(franquicia.getId(), franquicia.getNombre());
    }
}
