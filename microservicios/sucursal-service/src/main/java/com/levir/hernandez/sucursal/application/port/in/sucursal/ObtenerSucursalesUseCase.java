package com.levir.hernandez.sucursal.application.port.in.sucursal;

import com.levir.hernandez.sucursal.domain.model.Sucursal;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ObtenerSucursalesUseCase
{
    Flux<Sucursal> obtenerSucursales(UUID franquiciaId);
}
