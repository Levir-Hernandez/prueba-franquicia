package com.levir.hernandez.franquicia.application.port.in;

import com.levir.hernandez.franquicia.application.port.out.ProductoConMayorStock;

import java.util.List;
import java.util.UUID;

public interface ObtenerProductosConMayorStockUseCase
{
    /** Por cada sucursal de la franquicia, el producto con mas stock (si empatan, todos). */
    List<ProductoConMayorStock> obtenerProductosConMayorStock(UUID franquiciaId);
}
