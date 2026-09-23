package com.levir.hernandez.producto.application.port.out;

import com.levir.hernandez.producto.domain.model.Producto;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductoRepositoryPort
{
    Producto guardarProducto(Producto producto);
    Optional<Producto> obtenerProductoPorId(UUID productoId);
    void eliminarProductoPorId(UUID productoId);

    List<Producto> obtenerProductosPorIdDeSucursal(UUID sucursalId);

    /** Por cada sucursal indicada, el producto con mas stock (si empatan, todos). */
    List<Producto> obtenerProductosConMayorStockPorIdsDeSucursal(Collection<UUID> sucursalIds);
}
