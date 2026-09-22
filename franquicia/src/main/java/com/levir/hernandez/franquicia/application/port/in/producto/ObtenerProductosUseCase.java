package com.levir.hernandez.franquicia.application.port.in.producto;

import com.levir.hernandez.franquicia.domain.model.Producto;

import java.util.List;
import java.util.UUID;

public interface ObtenerProductosUseCase
{
    List<Producto> obtenerProductos(UUID sucursalId);
}
