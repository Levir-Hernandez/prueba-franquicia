package com.levir.hernandez.franquicia.application.port.out;

import com.levir.hernandez.franquicia.domain.model.Sucursal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SucursalRepositoryPort
{
    Sucursal guardarSucursal(Sucursal sucursal);
    Optional<Sucursal> obtenerSucursalPorId(UUID sucursalId);

    List<Sucursal> obtenerSucursalesPorIdDeFranquicia(UUID franquiciaId);
}
