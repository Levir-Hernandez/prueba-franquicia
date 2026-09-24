package com.levir.hernandez.producto.application.port.in;

import com.levir.hernandez.producto.application.port.out.ProductoConMayorStock;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ObtenerProductosConMayorStockUseCase
{
    /** Por cada sucursal de la franquicia, el producto con mas stock (si empatan, todos). */
    Flux<ProductoConMayorStock> obtenerProductosConMayorStock(UUID franquiciaId);
}
