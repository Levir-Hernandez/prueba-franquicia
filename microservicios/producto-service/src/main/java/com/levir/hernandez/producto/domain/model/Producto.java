package com.levir.hernandez.producto.domain.model;

import com.levir.hernandez.producto.domain.exception.ProductoInvalidoException;
import java.util.UUID;

public class Producto
{
    private final UUID id;
    private String nombre;
    private Integer stock;

    private UUID sucursalId;

    public Producto(UUID id, String nombre, Integer stock, UUID sucursalId)
    {
        this.id = id;
        renombrar(nombre);
        modificarStock(stock);
        this.sucursalId = sucursalId;
    }

    public Producto(String nombre, Integer stock, UUID sucursalId) {this(null, nombre, stock, sucursalId);}

    protected void validarNombre(String nombre)
    {
        if(nombre == null || nombre.isBlank())
        {
            throw new ProductoInvalidoException(
                    "El nombre del producto es obligatorio y no puede estar vacio"
            );
        }
    }

    protected void validarStock(Integer stock)
    {
        if(stock == null || stock < 0)
        {
            throw new ProductoInvalidoException("El stock del producto es obligatorio y no puede ser negativo");
        }
    }

    public void renombrar(String nombre)
    {
        validarNombre(nombre);
        this.nombre = nombre;
    }

    public void modificarStock(Integer stock)
    {
        validarStock(stock);
        this.stock = stock;
    }

    public UUID getId() {return id;}
    public String getNombre() {return nombre;}
    public Integer getStock() {return stock;}
    public UUID getSucursalId() {return sucursalId;}
}
