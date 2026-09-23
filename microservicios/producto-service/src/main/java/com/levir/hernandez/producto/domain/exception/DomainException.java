package com.levir.hernandez.producto.domain.exception;

/**
 * Excepcion base para identificar las violaciones de reglas del dominio
 */
public abstract class DomainException extends RuntimeException
{
    public DomainException(String message) {super(message);}
}
