package com.levir.hernandez.producto.application.service;

import com.levir.hernandez.producto.application.annotation.ObservableUseCase;
import com.levir.hernandez.producto.application.exception.ProductoNoEncontradoException;
import com.levir.hernandez.producto.application.port.in.producto.*;
import com.levir.hernandez.producto.application.port.out.ProductoRepositoryPort;
import com.levir.hernandez.producto.domain.model.Producto;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@ObservableUseCase
@RequiredArgsConstructor
public class ProductoService implements
        ObtenerProductoUseCase, ObtenerProductosUseCase, RenombrarProductoUseCase,
        ModificarStockProductoUseCase, EliminarProductoUseCase
{
    private final ProductoRepositoryPort productoRepositoryPort;

    @Override
    public void eliminarProducto(UUID productoId)
    {
        obtenerProducto(productoId);
        productoRepositoryPort.eliminarProductoPorId(productoId);
    }

    @Override
    public Producto modificarStockProducto(UUID productoId, Integer stock)
    {
        Producto producto = obtenerProducto(productoId);
        producto.modificarStock(stock);
        return productoRepositoryPort.guardarProducto(producto);
    }

    @Override
    public Producto obtenerProducto(UUID productoId)
    {
        return productoRepositoryPort.obtenerProductoPorId(productoId)
                .orElseThrow(() -> new ProductoNoEncontradoException(productoId));
    }

    @Override
    public List<Producto> obtenerProductos(UUID sucursalId)
    {
        return productoRepositoryPort.obtenerProductosPorIdDeSucursal(sucursalId);
    }

    @Override
    public Producto renombrarProducto(UUID productoId, String nombre)
    {
        Producto producto = obtenerProducto(productoId);
        producto.renombrar(nombre);
        return productoRepositoryPort.guardarProducto(producto);
    }
}
