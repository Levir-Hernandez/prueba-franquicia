package com.levir.hernandez.producto.application.port.out;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Consultas al servicio de sucursales
 */
public interface SucursalConsultaPort
{
    /** Emite ServicioNoDisponibleException si no es posible obtener la respuesta. */
    Mono<Boolean> existeSucursal(UUID sucursalId);

    /** Termina vacio si no es posible obtener la respuesta. */
    Flux<SucursalResumen> obtenerSucursalesDeFranquicia(UUID franquiciaId);
}
