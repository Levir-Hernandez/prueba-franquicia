package com.levir.hernandez.producto.infrastructure.adapter.in.web.mapper;

import com.levir.hernandez.producto.domain.model.Producto;
import com.levir.hernandez.producto.application.port.out.ProductoConMayorStock;
import com.levir.hernandez.producto.infrastructure.adapter.in.web.dto.response.ProductoConMayorStockResponse;
import com.levir.hernandez.producto.infrastructure.adapter.in.web.dto.response.ProductoResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Mapper encargado de convertir objetos de dominio en DTOs de respuesta
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DtoMapper
{
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
