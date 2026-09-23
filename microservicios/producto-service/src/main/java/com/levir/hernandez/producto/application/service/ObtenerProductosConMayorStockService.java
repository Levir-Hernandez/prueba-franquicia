package com.levir.hernandez.producto.application.service;

import com.levir.hernandez.producto.application.annotation.ObservableUseCase;
import com.levir.hernandez.producto.application.port.in.ObtenerProductosConMayorStockUseCase;
import com.levir.hernandez.producto.application.port.out.ProductoConMayorStock;
import com.levir.hernandez.producto.application.port.out.ProductoRepositoryPort;
import com.levir.hernandez.producto.application.port.out.SucursalConsultaPort;
import com.levir.hernandez.producto.application.port.out.SucursalResumen;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import java.util.UUID;

@ObservableUseCase
@RequiredArgsConstructor
public class ObtenerProductosConMayorStockService implements ObtenerProductosConMayorStockUseCase
{
    private final ProductoRepositoryPort productoRepositoryPort;
    private final SucursalConsultaPort sucursalConsultaPort;

    @Override
    public Flux<ProductoConMayorStock> obtenerProductosConMayorStock(UUID franquiciaId)
    {
        // Las sucursales viven en otro servicio: se consultan y luego se cruzan con los productos locales
        return sucursalConsultaPort.obtenerSucursalesDeFranquicia(franquiciaId)
                .collectMap(SucursalResumen::id, SucursalResumen::nombre)
                .filter(sucursales -> !sucursales.isEmpty())
                .flatMapMany(sucursales -> productoRepositoryPort
                        .obtenerProductosConMayorStockPorIdsDeSucursal(sucursales.keySet())
                        .map(producto -> new ProductoConMayorStock(producto.getId(), producto.getNombre(),
                                producto.getStock(), producto.getSucursalId(),
                                sucursales.get(producto.getSucursalId()))));
    }
}
