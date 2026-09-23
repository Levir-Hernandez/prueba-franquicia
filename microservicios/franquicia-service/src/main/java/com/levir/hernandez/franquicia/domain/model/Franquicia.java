package com.levir.hernandez.franquicia.domain.model;

import com.levir.hernandez.franquicia.domain.exception.FranquiciaInvalidaException;
import java.util.UUID;

public class Franquicia
{
    private final UUID id;
    private String nombre;

    public Franquicia(UUID id, String nombre)
    {
        this.id = id;
        renombrar(nombre);
    }

    public Franquicia(String nombre) {this(null, nombre);}

    protected void validarNombre(String nombre)
    {
        if(nombre == null || nombre.isBlank())
        {
            throw new FranquiciaInvalidaException(
                    "El nombre de la franquicia es obligatorio y no puede estar vacio"
            );
        }
    }

    public void renombrar(String nombre)
    {
        validarNombre(nombre);
        this.nombre = nombre;
    }

    public UUID getId() {return id;}
    public String getNombre() {return nombre;}
}
