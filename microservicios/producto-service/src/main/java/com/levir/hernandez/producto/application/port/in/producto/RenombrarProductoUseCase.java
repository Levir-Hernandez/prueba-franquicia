package com.levir.hernandez.producto.application.port.in.producto;

import com.levir.hernandez.producto.domain.model.Producto;

import java.util.UUID;

public interface RenombrarProductoUseCase
{
    Producto renombrarProducto(UUID productoId, String nombre);
}
