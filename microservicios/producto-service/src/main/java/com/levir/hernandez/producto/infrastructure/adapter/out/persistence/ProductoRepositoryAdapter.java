package com.levir.hernandez.producto.infrastructure.adapter.out.persistence;

import com.levir.hernandez.producto.application.port.out.ProductoRepositoryPort;
import com.levir.hernandez.producto.domain.model.Producto;
import com.levir.hernandez.producto.infrastructure.adapter.out.persistence.document.ProductoDocument;
import com.levir.hernandez.producto.infrastructure.adapter.out.persistence.repository.ProductoMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductoRepositoryAdapter implements ProductoRepositoryPort
{
    private final ProductoMongoRepository productoRepository;

    @Override
    public Producto guardarProducto(Producto producto)
    {
        // Mongo no genera UUIDs: si el producto es nuevo se le asigna uno aqui
        UUID id = Optional.ofNullable(producto.getId()).orElseGet(UUID::randomUUID);

        return toDomain(productoRepository.save(new ProductoDocument(
                id.toString(), producto.getNombre(), producto.getStock(), producto.getSucursalId().toString())));
    }

    @Override
    public Optional<Producto> obtenerProductoPorId(UUID productoId)
    {
        return productoRepository.findById(productoId.toString())
                .map(ProductoRepositoryAdapter::toDomain);
    }

    @Override
    public List<Producto> obtenerProductosPorIdDeSucursal(UUID sucursalId)
    {
        return productoRepository.findBySucursalId(sucursalId.toString()).stream()
                .map(ProductoRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public void eliminarProductoPorId(UUID productoId)
    {
        productoRepository.deleteById(productoId.toString());
    }

    @Override
    public List<Producto> obtenerProductosConMayorStockPorIdsDeSucursal(Collection<UUID> sucursalIds)
    {
        List<String> ids = sucursalIds.stream().map(UUID::toString).toList();

        // Agrupa por sucursal y conserva los productos que igualan el stock maximo de su sucursal
        return productoRepository.findBySucursalIdIn(ids).stream()
                .collect(Collectors.groupingBy(ProductoDocument::getSucursalId))
                .values().stream()
                .flatMap(productos ->
                {
                    int maximo = productos.stream().mapToInt(ProductoDocument::getStock).max().orElseThrow();
                    return productos.stream().filter(producto -> producto.getStock() == maximo);
                })
                .map(ProductoRepositoryAdapter::toDomain)
                .toList();
    }

    private static Producto toDomain(ProductoDocument document)
    {
        return new Producto(UUID.fromString(document.getId()), document.getNombre(), document.getStock(),
                UUID.fromString(document.getSucursalId()));
    }
}
