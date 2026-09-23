package com.levir.hernandez.producto.application.port.out;

import com.levir.hernandez.producto.domain.model.Producto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.UUID;

public interface ProductoRepositoryPort
{
    Mono<Producto> guardarProducto(Producto producto);
    /** Vacio si el producto no existe. */
    Mono<Producto> obtenerProductoPorId(UUID productoId);
    Mono<Void> eliminarProductoPorId(UUID productoId);

    Flux<Producto> obtenerProductosPorIdDeSucursal(UUID sucursalId);

    /** Por cada sucursal indicada, el producto con mas stock (si empatan, todos). */
    Flux<Producto> obtenerProductosConMayorStockPorIdsDeSucursal(Collection<UUID> sucursalIds);
}
