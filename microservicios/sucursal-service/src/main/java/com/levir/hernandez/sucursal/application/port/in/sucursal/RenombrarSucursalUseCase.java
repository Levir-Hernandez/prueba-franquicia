package com.levir.hernandez.sucursal.application.port.in.sucursal;

import com.levir.hernandez.sucursal.domain.model.Sucursal;

import java.util.UUID;

public interface RenombrarSucursalUseCase
{
    Sucursal renombrarSucursal(UUID sucursalId, String nombre);
}
