package com.levir.hernandez.sucursal.application.exception;

/**
 * Otro microservicio del que depende la operacion no respondio a tiempo o su circuito esta abierto
 */
public class ServicioNoDisponibleException extends RuntimeException
{
    public ServicioNoDisponibleException(String servicio)
    {
        super("El servicio de " + servicio + " no esta disponible, intente mas tarde");
    }
}
