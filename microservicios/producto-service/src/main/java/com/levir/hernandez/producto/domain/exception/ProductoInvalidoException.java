package com.levir.hernandez.producto.domain.exception;

/**
 * Excepción que identifica un producto que no cumple las reglas del dominio.
 * Se produce cuando:
 * El nombre es nulo, vacio o esta ausente.
 * El stock es nulo o negativo.
 */
public class ProductoInvalidoException extends DomainException
{
    public ProductoInvalidoException(String message)
    {
        super(message);
    }
}
