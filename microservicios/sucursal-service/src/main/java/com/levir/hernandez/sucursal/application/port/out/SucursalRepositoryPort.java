package com.levir.hernandez.sucursal.application.port.out;

import com.levir.hernandez.sucursal.domain.model.Sucursal;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface SucursalRepositoryPort
{
    Mono<Sucursal> guardarSucursal(Sucursal sucursal);
    /** Vacio si la sucursal no existe. */
    Mono<Sucursal> obtenerSucursalPorId(UUID sucursalId);

    Flux<Sucursal> obtenerSucursalesPorIdDeFranquicia(UUID franquiciaId);
}
