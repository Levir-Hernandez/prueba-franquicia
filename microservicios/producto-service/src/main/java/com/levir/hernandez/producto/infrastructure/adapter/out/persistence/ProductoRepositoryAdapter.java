package com.levir.hernandez.producto.infrastructure.adapter.out.persistence;

import com.levir.hernandez.producto.application.port.out.ProductoRepositoryPort;
import com.levir.hernandez.producto.domain.model.Producto;
import com.levir.hernandez.producto.infrastructure.adapter.out.persistence.document.ProductoDocument;
import com.levir.hernandez.producto.infrastructure.adapter.out.persistence.repository.ProductoMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Component
@RequiredArgsConstructor
public class ProductoRepositoryAdapter implements ProductoRepositoryPort
{
    private final ProductoMongoRepository productoRepository;
    private final ReactiveMongoTemplate mongoTemplate;

    @Override
    public Mono<Producto> guardarProducto(Producto producto)
    {
        // Mongo no genera UUIDs: si el producto es nuevo se le asigna uno aqui
        UUID id = Optional.ofNullable(producto.getId()).orElseGet(UUID::randomUUID);

        return productoRepository.save(new ProductoDocument(
                        id.toString(), producto.getNombre(), producto.getStock(), producto.getSucursalId().toString()))
                .map(ProductoRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<Producto> obtenerProductoPorId(UUID productoId)
    {
        return productoRepository.findById(productoId.toString())
                .map(ProductoRepositoryAdapter::toDomain);
    }

    @Override
    public Flux<Producto> obtenerProductosPorIdDeSucursal(UUID sucursalId)
    {
        return productoRepository.findBySucursalId(sucursalId.toString())
                .map(ProductoRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<Void> eliminarProductoPorId(UUID productoId)
    {
        return productoRepository.deleteById(productoId.toString());
    }

    @Override
    public Flux<Producto> obtenerProductosConMayorStockPorIdsDeSucursal(Collection<UUID> sucursalIds)
    {
        // El calculo se hace en Mongo: agrupa por sucursal y conserva los productos que igualan su stock maximo
        Aggregation mayorStockPorSucursal = newAggregation(
                match(Criteria.where("sucursalId").in(sucursalIds.stream().map(UUID::toString).toList())),
                group("sucursalId").max("stock").as("maximo").push(ROOT).as("productos"),
                unwind("productos"),
                match(Criteria.expr(ComparisonOperators.valueOf("productos.stock").equalTo("maximo"))),
                replaceRoot("productos"));

        return mongoTemplate.aggregate(mayorStockPorSucursal,
                        mongoTemplate.getCollectionName(ProductoDocument.class), ProductoDocument.class)
                .map(ProductoRepositoryAdapter::toDomain);
    }

    private static Producto toDomain(ProductoDocument document)
    {
        return new Producto(UUID.fromString(document.getId()), document.getNombre(), document.getStock(),
                UUID.fromString(document.getSucursalId()));
    }
}
