package com.levir.hernandez.sucursal.domain.model;

import com.levir.hernandez.sucursal.domain.exception.SucursalInvalidaException;
import java.util.UUID;

public class Sucursal
{
    private final UUID id;
    private String nombre;

    private UUID franquiciaId;

    public Sucursal(UUID id, String nombre, UUID franquiciaId)
    {
        this.id = id;
        renombrar(nombre);
        this.franquiciaId = franquiciaId;
    }

    public Sucursal(String nombre, UUID franquiciaId) {this(null, nombre, franquiciaId);}

    protected void validarNombre(String nombre)
    {
        if(nombre == null || nombre.isBlank())
        {
            throw new SucursalInvalidaException(
                    "El nombre de la sucursal es obligatorio y no puede estar vacio"
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
    public UUID getFranquiciaId() {return franquiciaId;}
}
