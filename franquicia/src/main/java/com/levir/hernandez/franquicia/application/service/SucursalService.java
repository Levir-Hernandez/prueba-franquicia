package com.levir.hernandez.franquicia.application.service;

import com.levir.hernandez.franquicia.application.annotation.ObservableUseCase;
import com.levir.hernandez.franquicia.application.exception.SucursalNoEncontradaException;
import com.levir.hernandez.franquicia.application.port.in.sucursal.*;
import com.levir.hernandez.franquicia.application.port.out.SucursalRepositoryPort;
import com.levir.hernandez.franquicia.domain.model.Sucursal;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@ObservableUseCase
@RequiredArgsConstructor
public class SucursalService implements
        ObtenerSucursalUseCase, ObtenerSucursalesUseCase,
        RenombrarSucursalUseCase, AgregarSucursalUseCase
{
    private final SucursalRepositoryPort sucursalRepositoryPort;

    @Override
    public Sucursal agregarSucursal(UUID franquiciaId, String nombre)
    {
        return sucursalRepositoryPort.guardarSucursal(new Sucursal(nombre, franquiciaId));
    }

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
