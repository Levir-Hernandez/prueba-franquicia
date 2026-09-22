package com.levir.hernandez.franquicia.application.service;

import com.levir.hernandez.franquicia.application.annotation.ObservableUseCase;
import com.levir.hernandez.franquicia.application.port.in.ObtenerProductosConMayorStockUseCase;
import com.levir.hernandez.franquicia.application.port.out.ProductoRepositoryPort;
import com.levir.hernandez.franquicia.application.port.out.ProductoConMayorStock;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@ObservableUseCase
@RequiredArgsConstructor
public class ObtenerProductosConMayorStockService implements ObtenerProductosConMayorStockUseCase
{
    private final ProductoRepositoryPort productoRepositoryPort;

    @Override
    public List<ProductoConMayorStock> obtenerProductosConMayorStock(UUID franquiciaId)
    {
        return productoRepositoryPort.obtenerProductosConMayorStockPorIdDeFranquicia(franquiciaId);
    }
}
