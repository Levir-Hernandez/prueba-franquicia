package com.levir.hernandez.producto.application.port.in.producto;

import com.levir.hernandez.producto.domain.model.Producto;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ObtenerProductosUseCase
{
    Flux<Producto> obtenerProductos(UUID sucursalId);
}
