package com.levir.hernandez.sucursal.application.port.out;

import java.util.UUID;

/**
 * Consultas al servicio de franquicias.
 * Lanza ServicioNoDisponibleException si no es posible obtener la respuesta.
 */
public interface FranquiciaConsultaPort
{
    boolean existeFranquicia(UUID franquiciaId);
}
