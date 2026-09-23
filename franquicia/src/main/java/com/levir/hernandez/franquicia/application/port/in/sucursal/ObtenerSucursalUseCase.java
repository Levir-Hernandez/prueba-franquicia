package com.levir.hernandez.franquicia.application.port.in.sucursal;

import com.levir.hernandez.franquicia.domain.model.Sucursal;

import java.util.UUID;

public interface ObtenerSucursalUseCase
{
    Sucursal obtenerSucursal(UUID sucursalId);
}
