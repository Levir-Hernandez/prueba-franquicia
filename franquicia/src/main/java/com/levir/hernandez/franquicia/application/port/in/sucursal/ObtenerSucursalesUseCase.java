package com.levir.hernandez.franquicia.application.port.in.sucursal;

import com.levir.hernandez.franquicia.domain.model.Sucursal;

import java.util.List;
import java.util.UUID;

public interface ObtenerSucursalesUseCase
{
    List<Sucursal> obtenerSucursales(UUID franquiciaId);
}
