package com.levir.hernandez.sucursal.application.service;

import com.levir.hernandez.sucursal.application.annotation.ObservableUseCase;
import com.levir.hernandez.sucursal.application.exception.FranquiciaNoEncontradaException;
import com.levir.hernandez.sucursal.application.port.in.sucursal.AgregarSucursalUseCase;
import com.levir.hernandez.sucursal.application.port.out.FranquiciaConsultaPort;
import com.levir.hernandez.sucursal.application.port.out.SucursalRepositoryPort;
import com.levir.hernandez.sucursal.domain.model.Sucursal;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Unico caso de uso de sucursales que depende de otro servicio: valida que la franquicia exista
 */
@ObservableUseCase
@RequiredArgsConstructor
public class AgregarSucursalService implements AgregarSucursalUseCase
{
    private final SucursalRepositoryPort sucursalRepositoryPort;
    private final FranquiciaConsultaPort franquiciaConsultaPort;

    @Override
    public Sucursal agregarSucursal(UUID franquiciaId, String nombre)
    {
        // Se validan las reglas del dominio antes de consultar a otro servicio
        Sucursal sucursal = new Sucursal(nombre, franquiciaId);

        if (!franquiciaConsultaPort.existeFranquicia(franquiciaId))
        {
            throw new FranquiciaNoEncontradaException(franquiciaId);
        }

        return sucursalRepositoryPort.guardarSucursal(sucursal);
    }
}
