package com.levir.hernandez.sucursal.application.service;

import com.levir.hernandez.sucursal.application.annotation.ObservableUseCase;
import com.levir.hernandez.sucursal.application.exception.SucursalNoEncontradaException;
import com.levir.hernandez.sucursal.application.port.in.sucursal.ObtenerSucursalUseCase;
import com.levir.hernandez.sucursal.application.port.in.sucursal.ObtenerSucursalesUseCase;
import com.levir.hernandez.sucursal.application.port.in.sucursal.RenombrarSucursalUseCase;
import com.levir.hernandez.sucursal.application.port.out.SucursalRepositoryPort;
import com.levir.hernandez.sucursal.domain.model.Sucursal;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@ObservableUseCase
@RequiredArgsConstructor
public class SucursalService implements
        ObtenerSucursalUseCase, ObtenerSucursalesUseCase, RenombrarSucursalUseCase
{
    private final SucursalRepositoryPort sucursalRepositoryPort;

    @Override
    public Mono<Sucursal> obtenerSucursal(UUID sucursalId)
    {
        return sucursalRepositoryPort.obtenerSucursalPorId(sucursalId)
                .switchIfEmpty(Mono.error(() -> new SucursalNoEncontradaException(sucursalId)));
    }

    @Override
    public Flux<Sucursal> obtenerSucursales(UUID franquiciaId)
    {
        return sucursalRepositoryPort.obtenerSucursalesPorIdDeFranquicia(franquiciaId);
    }

    @Override
    public Mono<Sucursal> renombrarSucursal(UUID sucursalId, String nombre)
    {
        return obtenerSucursal(sucursalId)
                .map(sucursal ->
                {
                    sucursal.renombrar(nombre);
                    return sucursal;
                })
                .flatMap(sucursalRepositoryPort::guardarSucursal);
    }
}
