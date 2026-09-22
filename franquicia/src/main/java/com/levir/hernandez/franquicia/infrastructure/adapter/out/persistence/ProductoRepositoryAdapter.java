package com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence;

import com.levir.hernandez.franquicia.application.port.out.ProductoRepositoryPort;
import com.levir.hernandez.franquicia.application.port.out.ProductoConMayorStock;
import com.levir.hernandez.franquicia.domain.model.Producto;
import com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.entity.ProductoEntity;
import com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.entity.SucursalEntity;
import com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.mapper.EntityMapper;
import com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.repository.ProductoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductoRepositoryAdapter implements ProductoRepositoryPort
{
    private final ProductoJpaRepository productoRepository;

    @Override
    public Producto guardarProducto(Producto producto)
    {
        // Recupera la entidad existente para actualizarla o crea una nueva si no existe
        ProductoEntity entity = Optional.ofNullable(producto.getId())
                .flatMap(productoRepository::findById)
                .orElseGet(ProductoEntity::new);

        entity.setNombre(producto.getNombre());
        entity.setStock(producto.getStock());
        entity.setSucursal(new SucursalEntity(producto.getSucursalId()));

        return EntityMapper.toDomain(productoRepository.save(entity));
    }

    @Override
    public Optional<Producto> obtenerProductoPorId(UUID productoId)
    {
        return productoRepository.findById(productoId)
                .map(EntityMapper::toDomain);
    }

    @Override
    public List<Producto> obtenerProductosPorIdDeSucursal(UUID sucursalId)
    {
        return productoRepository.findBySucursalId(sucursalId).stream()
                .map(EntityMapper::toDomain)
                .toList();
    }

    @Override
    public void eliminarProductoPorId(UUID productoId)
    {
        productoRepository.deleteById(productoId);
    }

    @Override
    public List<ProductoConMayorStock> obtenerProductosConMayorStockPorIdDeFranquicia(UUID franquiciaId)
    {
        return productoRepository.obtenerProductosConMayorStockPorIdDeFranquicia(franquiciaId);
    }
}
