package com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.mapper;

import com.levir.hernandez.franquicia.domain.model.Franquicia;
import com.levir.hernandez.franquicia.domain.model.Producto;
import com.levir.hernandez.franquicia.domain.model.Sucursal;
import com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.entity.FranquiciaEntity;
import com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.entity.ProductoEntity;
import com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.entity.SucursalEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Mapper encargado de convertir entidades de persistencia JPA en objetos de dominio
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EntityMapper
{
    public static Sucursal toDomain(SucursalEntity entity)
    {
        return new Sucursal(entity.getId(), entity.getNombre(), entity.getFranquicia().getId());
    }

    public static Producto toDomain(ProductoEntity entity)
    {
        return new Producto(entity.getId(), entity.getNombre(), entity.getStock(), entity.getSucursal().getId());
    }

    public static Franquicia toDomain(FranquiciaEntity entity)
    {
        return new Franquicia(entity.getId(), entity.getNombre());
    }
}
