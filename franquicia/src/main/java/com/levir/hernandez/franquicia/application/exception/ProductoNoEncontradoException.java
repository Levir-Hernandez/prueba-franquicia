package com.levir.hernandez.franquicia.application.exception;

import java.util.UUID;

public class ProductoNoEncontradoException extends RecursoNoEncontradoException
{
    public ProductoNoEncontradoException(UUID id)
    {
        super("No existe el producto " + id);
    }
}
