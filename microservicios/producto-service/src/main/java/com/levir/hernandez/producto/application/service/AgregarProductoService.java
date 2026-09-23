package com.levir.hernandez.producto.application.service;

import com.levir.hernandez.producto.application.annotation.ObservableUseCase;
import com.levir.hernandez.producto.application.exception.SucursalNoEncontradaException;
import com.levir.hernandez.producto.application.port.in.producto.AgregarProductoUseCase;
import com.levir.hernandez.producto.application.port.out.ProductoRepositoryPort;
import com.levir.hernandez.producto.application.port.out.SucursalConsultaPort;
import com.levir.hernandez.producto.domain.model.Producto;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Caso de uso que depende de otro servicio: valida que la sucursal exista antes de agregar el producto
 */
@ObservableUseCase
@RequiredArgsConstructor
public class AgregarProductoService implements AgregarProductoUseCase
{
    private final ProductoRepositoryPort productoRepositoryPort;
    private final SucursalConsultaPort sucursalConsultaPort;

    @Override
    public Producto agregarProducto(UUID sucursalId, String nombre, Integer stock)
    {
        // Se validan las reglas del dominio antes de consultar a otro servicio
        Producto producto = new Producto(nombre, stock, sucursalId);

        if (!sucursalConsultaPort.existeSucursal(sucursalId))
        {
            throw new SucursalNoEncontradaException(sucursalId);
        }

        return productoRepositoryPort.guardarProducto(producto);
    }
}
