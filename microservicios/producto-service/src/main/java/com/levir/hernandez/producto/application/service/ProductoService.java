package com.levir.hernandez.producto.application.service;

import com.levir.hernandez.producto.application.annotation.ObservableUseCase;
import com.levir.hernandez.producto.application.exception.ProductoNoEncontradoException;
import com.levir.hernandez.producto.application.port.in.producto.*;
import com.levir.hernandez.producto.application.port.out.ProductoRepositoryPort;
import com.levir.hernandez.producto.domain.model.Producto;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@ObservableUseCase
@RequiredArgsConstructor
public class ProductoService implements
        ObtenerProductoUseCase, ObtenerProductosUseCase, RenombrarProductoUseCase,
        ModificarStockProductoUseCase, EliminarProductoUseCase
{
    private final ProductoRepositoryPort productoRepositoryPort;

    @Override
    public Mono<Void> eliminarProducto(UUID productoId)
    {
        return obtenerProducto(productoId)
                .flatMap(producto -> productoRepositoryPort.eliminarProductoPorId(productoId));
    }

    @Override
    public Mono<Producto> modificarStockProducto(UUID productoId, Integer stock)
    {
        return obtenerProducto(productoId)
                .map(producto ->
                {
                    producto.modificarStock(stock);
                    return producto;
                })
                .flatMap(productoRepositoryPort::guardarProducto);
    }

    @Override
    public Mono<Producto> obtenerProducto(UUID productoId)
    {
        return productoRepositoryPort.obtenerProductoPorId(productoId)
                .switchIfEmpty(Mono.error(() -> new ProductoNoEncontradoException(productoId)));
    }

    @Override
    public Flux<Producto> obtenerProductos(UUID sucursalId)
    {
        return productoRepositoryPort.obtenerProductosPorIdDeSucursal(sucursalId);
    }

    @Override
    public Mono<Producto> renombrarProducto(UUID productoId, String nombre)
    {
        return obtenerProducto(productoId)
                .map(producto ->
                {
                    producto.renombrar(nombre);
                    return producto;
                })
                .flatMap(productoRepositoryPort::guardarProducto);
    }
}
