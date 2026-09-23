package com.levir.hernandez.producto.application.port.in.producto;

import com.levir.hernandez.producto.domain.model.Producto;

import java.util.List;
import java.util.UUID;

public interface ObtenerProductosUseCase
{
    List<Producto> obtenerProductos(UUID sucursalId);
}
