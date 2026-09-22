package com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.repository;

import com.levir.hernandez.franquicia.application.port.out.ProductoConMayorStock;
import com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.entity.ProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProductoJpaRepository extends JpaRepository<ProductoEntity, UUID>
{
    List<ProductoEntity> findBySucursalId(UUID sucursalId);

    /**
     * Obtiene los productos con mayor stock de cada sucursal de la franquicia.
     * Incluye todos los productos que empaten en el stock máximo y excluye las sucursales sin productos.
     */
    @Query("""
            select new com.levir.hernandez.franquicia.application.port.out.ProductoConMayorStock(
                       p.id, p.nombre, p.stock, s.id, s.nombre)
            from ProductoEntity p
                join p.sucursal s
            where s.franquicia.id = :franquiciaId
              and p.stock = (select max(otro.stock) from ProductoEntity otro where otro.sucursal = s)
            """)
    List<ProductoConMayorStock> obtenerProductosConMayorStockPorIdDeFranquicia(@Param("franquiciaId") UUID franquiciaId);
}
