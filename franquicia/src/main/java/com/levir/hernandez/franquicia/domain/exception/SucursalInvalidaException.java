package com.levir.hernandez.franquicia.domain.exception;

/**
 * Excepción que identifica una sucursal que no cumple las reglas del dominio.
 * Se produce cuando:
 * El nombre es nulo, vacio o esta ausente.
 */
public class SucursalInvalidaException extends DomainException
{
    public SucursalInvalidaException(String message)
    {
        super(message);
    }
}
