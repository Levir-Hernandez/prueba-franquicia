package com.levir.hernandez.producto.application.exception;

/**
 * Excepcion base para identificar recursos solicitados que no existen
 */
public abstract class RecursoNoEncontradoException extends RuntimeException
{
    public RecursoNoEncontradoException(String message) {super(message);}
}
