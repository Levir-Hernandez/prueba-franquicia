package com.levir.hernandez.franquicia.infrastructure.adapter.in.web.mapper;

import com.levir.hernandez.franquicia.domain.model.Franquicia;
import com.levir.hernandez.franquicia.domain.model.Producto;
import com.levir.hernandez.franquicia.application.port.out.ProductoConMayorStock;
import com.levir.hernandez.franquicia.domain.model.Sucursal;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.dto.response.FranquiciaResponse;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.dto.response.ProductoConMayorStockResponse;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.dto.response.ProductoResponse;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.dto.response.SucursalResponse;
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

    public static SucursalResponse toResponse(Sucursal sucursal)
    {
        return new SucursalResponse(sucursal.getId(), sucursal.getNombre(), sucursal.getFranquiciaId());
    }

    public static ProductoResponse toResponse(Producto producto)
    {
        return new ProductoResponse(producto.getId(), producto.getNombre(), producto.getStock(), producto.getSucursalId());
    }

    public static ProductoConMayorStockResponse toResponse(ProductoConMayorStock producto)
    {
        return new ProductoConMayorStockResponse(producto.sucursalId(), producto.sucursalNombre(),
                producto.productoId(), producto.productoNombre(), producto.stock());
    }
}
