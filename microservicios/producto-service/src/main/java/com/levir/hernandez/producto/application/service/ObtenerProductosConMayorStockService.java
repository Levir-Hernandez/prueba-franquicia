package com.levir.hernandez.producto.application.service;

import com.levir.hernandez.producto.application.annotation.ObservableUseCase;
import com.levir.hernandez.producto.application.port.in.ObtenerProductosConMayorStockUseCase;
import com.levir.hernandez.producto.application.port.out.ProductoConMayorStock;
import com.levir.hernandez.producto.application.port.out.ProductoRepositoryPort;
import com.levir.hernandez.producto.application.port.out.SucursalConsultaPort;
import com.levir.hernandez.producto.application.port.out.SucursalResumen;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@ObservableUseCase
@RequiredArgsConstructor
public class ObtenerProductosConMayorStockService implements ObtenerProductosConMayorStockUseCase
{
    private final ProductoRepositoryPort productoRepositoryPort;
    private final SucursalConsultaPort sucursalConsultaPort;

    @Override
    public List<ProductoConMayorStock> obtenerProductosConMayorStock(UUID franquiciaId)
    {
        // Las sucursales viven en otro servicio: se consultan y luego se cruzan con los productos locales
        Map<UUID, String> sucursales = sucursalConsultaPort.obtenerSucursalesDeFranquicia(franquiciaId).stream()
                .collect(Collectors.toMap(SucursalResumen::id, SucursalResumen::nombre, (a, b) -> a));

        if (sucursales.isEmpty()) return List.of();

        return productoRepositoryPort.obtenerProductosConMayorStockPorIdsDeSucursal(sucursales.keySet()).stream()
                .map(producto -> new ProductoConMayorStock(producto.getId(), producto.getNombre(), producto.getStock(),
                        producto.getSucursalId(), sucursales.get(producto.getSucursalId())))
                .toList();
    }
}
