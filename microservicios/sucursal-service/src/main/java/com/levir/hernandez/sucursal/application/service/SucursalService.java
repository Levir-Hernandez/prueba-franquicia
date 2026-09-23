package com.levir.hernandez.sucursal.application.service;

import com.levir.hernandez.sucursal.application.annotation.ObservableUseCase;
import com.levir.hernandez.sucursal.application.exception.SucursalNoEncontradaException;
import com.levir.hernandez.sucursal.application.port.in.sucursal.ObtenerSucursalUseCase;
import com.levir.hernandez.sucursal.application.port.in.sucursal.ObtenerSucursalesUseCase;
import com.levir.hernandez.sucursal.application.port.in.sucursal.RenombrarSucursalUseCase;
import com.levir.hernandez.sucursal.application.port.out.SucursalRepositoryPort;
import com.levir.hernandez.sucursal.domain.model.Sucursal;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@ObservableUseCase
@RequiredArgsConstructor
public class SucursalService implements
        ObtenerSucursalUseCase, ObtenerSucursalesUseCase, RenombrarSucursalUseCase
{
    private final SucursalRepositoryPort sucursalRepositoryPort;

    @Override
    public Sucursal obtenerSucursal(UUID sucursalId)
    {
        return sucursalRepositoryPort.obtenerSucursalPorId(sucursalId)
                .orElseThrow(() -> new SucursalNoEncontradaException(sucursalId));
    }

    @Override
    public List<Sucursal> obtenerSucursales(UUID franquiciaId)
    {
        return sucursalRepositoryPort.obtenerSucursalesPorIdDeFranquicia(franquiciaId);
    }

    @Override
    public Sucursal renombrarSucursal(UUID sucursalId, String nombre)
    {
        Sucursal sucursal = obtenerSucursal(sucursalId);
        sucursal.renombrar(nombre);
        return sucursalRepositoryPort.guardarSucursal(sucursal);
    }
}
