package com.levir.hernandez.sucursal.application.port.out;

import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Consultas al servicio de franquicias.
 * Emite ServicioNoDisponibleException si no es posible obtener la respuesta.
 */
public interface FranquiciaConsultaPort
{
    Mono<Boolean> existeFranquicia(UUID franquiciaId);
}
