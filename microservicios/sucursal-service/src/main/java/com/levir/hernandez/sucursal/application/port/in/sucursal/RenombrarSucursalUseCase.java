package com.levir.hernandez.sucursal.application.port.in.sucursal;

import com.levir.hernandez.sucursal.domain.model.Sucursal;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RenombrarSucursalUseCase
{
    Mono<Sucursal> renombrarSucursal(UUID sucursalId, String nombre);
}
