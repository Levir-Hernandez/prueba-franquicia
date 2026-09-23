package com.levir.hernandez.sucursal.application.port.in.sucursal;

import com.levir.hernandez.sucursal.domain.model.Sucursal;

import java.util.List;
import java.util.UUID;

public interface ObtenerSucursalesUseCase
{
    List<Sucursal> obtenerSucursales(UUID franquiciaId);
}
