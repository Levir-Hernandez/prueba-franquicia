package com.levir.hernandez.franquicia.application.port.in.producto;

import com.levir.hernandez.franquicia.domain.model.Producto;

import java.util.UUID;

public interface ModificarStockProductoUseCase
{
    Producto modificarStockProducto(UUID productoId, Integer stock);
}
