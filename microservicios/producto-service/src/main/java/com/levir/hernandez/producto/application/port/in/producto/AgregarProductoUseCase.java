package com.levir.hernandez.producto.application.port.in.producto;

import com.levir.hernandez.producto.domain.model.Producto;

import java.util.UUID;

public interface AgregarProductoUseCase
{
    Producto agregarProducto(UUID sucursalId, String nombre, Integer stock);
}
