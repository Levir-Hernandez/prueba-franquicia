package com.levir.hernandez.producto.infrastructure.adapter.out.persistence.repository;

import com.levir.hernandez.producto.infrastructure.adapter.out.persistence.document.ProductoDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface ProductoMongoRepository extends MongoRepository<ProductoDocument, String>
{
    List<ProductoDocument> findBySucursalId(String sucursalId);

    List<ProductoDocument> findBySucursalIdIn(Collection<String> sucursalIds);
}
