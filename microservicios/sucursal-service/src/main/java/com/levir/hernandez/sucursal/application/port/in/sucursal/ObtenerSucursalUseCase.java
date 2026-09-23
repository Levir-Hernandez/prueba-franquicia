package com.levir.hernandez.sucursal.application.port.in.sucursal;

import com.levir.hernandez.sucursal.domain.model.Sucursal;

import java.util.UUID;

public interface ObtenerSucursalUseCase
{
    Sucursal obtenerSucursal(UUID sucursalId);
}
