package com.levir.hernandez.franquicia.application.port.in.producto;

import java.util.UUID;

public interface EliminarProductoUseCase
{
    void eliminarProducto(UUID productoId);
}
