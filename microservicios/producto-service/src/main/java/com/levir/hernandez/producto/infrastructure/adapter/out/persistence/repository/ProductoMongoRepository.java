package com.levir.hernandez.producto.infrastructure.adapter.out.persistence.repository;

import com.levir.hernandez.producto.infrastructure.adapter.out.persistence.document.ProductoDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ProductoMongoRepository extends ReactiveMongoRepository<ProductoDocument, String>
{
    Flux<ProductoDocument> findBySucursalId(String sucursalId);
}
