package com.levir.hernandez.franquicia.application.port.out;

import com.levir.hernandez.franquicia.domain.model.Producto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductoRepositoryPort
{
    Producto guardarProducto(Producto producto);
    Optional<Producto> obtenerProductoPorId(UUID productoId);
    void eliminarProductoPorId(UUID productoId);

    List<Producto> obtenerProductosPorIdDeSucursal(UUID sucursalId);
    List<ProductoConMayorStock> obtenerProductosConMayorStockPorIdDeFranquicia(UUID franquiciaId);
}
