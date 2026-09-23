package com.levir.hernandez.franquicia.application.port.in.producto;

import com.levir.hernandez.franquicia.domain.model.Producto;

import java.util.UUID;

public interface RenombrarProductoUseCase
{
    Producto renombrarProducto(UUID productoId, String nombre);
}
