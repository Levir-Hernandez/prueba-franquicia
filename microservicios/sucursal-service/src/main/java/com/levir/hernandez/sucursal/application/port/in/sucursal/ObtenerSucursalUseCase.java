package com.levir.hernandez.sucursal.application.port.in.sucursal;

import com.levir.hernandez.sucursal.domain.model.Sucursal;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ObtenerSucursalUseCase
{
    Mono<Sucursal> obtenerSucursal(UUID sucursalId);
}
