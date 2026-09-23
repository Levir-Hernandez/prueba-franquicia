package com.levir.hernandez.franquicia.domain.exception;

/**
 * Excepción que identifica una franquicia que no cumple las reglas del dominio.
 * Se produce cuando:
 * El nombre es nulo, vacio o esta ausente.
 */
public class FranquiciaInvalidaException extends DomainException
{
    public FranquiciaInvalidaException(String message)
    {
        super(message);
    }
}
