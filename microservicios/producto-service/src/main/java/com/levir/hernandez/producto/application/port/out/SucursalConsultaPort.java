package com.levir.hernandez.producto.application.port.out;

import java.util.List;
import java.util.UUID;

/**
 * Consultas al servicio de sucursales
 */
public interface SucursalConsultaPort
{
    /** Lanza ServicioNoDisponibleException si no es posible obtener la respuesta. */
    boolean existeSucursal(UUID sucursalId);

    /** Devuelve una lista vacia si no es posible obtener la respuesta. */
    List<SucursalResumen> obtenerSucursalesDeFranquicia(UUID franquiciaId);
}
