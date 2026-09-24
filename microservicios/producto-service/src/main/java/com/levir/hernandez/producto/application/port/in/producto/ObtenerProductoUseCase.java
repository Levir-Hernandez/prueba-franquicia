package com.levir.hernandez.producto.application.port.in.producto;

import com.levir.hernandez.producto.domain.model.Producto;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ObtenerProductoUseCase
{
    Mono<Producto> obtenerProducto(UUID productoId);
}
