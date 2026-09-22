package com.levir.hernandez.franquicia.application.exception;

import java.util.UUID;

public class SucursalNoEncontradaException extends RecursoNoEncontradoException
{
    public SucursalNoEncontradaException(UUID id)
    {
        super("No existe la sucursal " + id);
    }
}
