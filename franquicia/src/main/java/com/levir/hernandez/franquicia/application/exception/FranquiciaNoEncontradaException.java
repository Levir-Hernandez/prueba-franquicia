package com.levir.hernandez.franquicia.application.exception;

import java.util.UUID;

public class FranquiciaNoEncontradaException extends RecursoNoEncontradoException
{
    public FranquiciaNoEncontradaException(UUID id)
    {
        super("No existe la franquicia " + id);
    }
}
