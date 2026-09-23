package com.levir.hernandez.producto.application.port.in.producto;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface EliminarProductoUseCase
{
    Mono<Void> eliminarProducto(UUID productoId);
}
