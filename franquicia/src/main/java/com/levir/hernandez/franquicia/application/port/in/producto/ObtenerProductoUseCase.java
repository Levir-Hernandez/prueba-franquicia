package com.levir.hernandez.franquicia.application.port.in.producto;

import com.levir.hernandez.franquicia.domain.model.Producto;

import java.util.UUID;

public interface ObtenerProductoUseCase
{
    Producto obtenerProducto(UUID productoId);
}
