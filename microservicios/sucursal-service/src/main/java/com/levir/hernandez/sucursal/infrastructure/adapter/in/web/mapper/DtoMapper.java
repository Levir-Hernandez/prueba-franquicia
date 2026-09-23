package com.levir.hernandez.sucursal.infrastructure.adapter.in.web.mapper;

import com.levir.hernandez.sucursal.domain.model.Sucursal;
import com.levir.hernandez.sucursal.infrastructure.adapter.in.web.dto.response.SucursalResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Mapper encargado de convertir objetos de dominio en DTOs de respuesta
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DtoMapper
{
    public static SucursalResponse toResponse(Sucursal sucursal)
    {
        return new SucursalResponse(sucursal.getId(), sucursal.getNombre(), sucursal.getFranquiciaId());
    }
}
