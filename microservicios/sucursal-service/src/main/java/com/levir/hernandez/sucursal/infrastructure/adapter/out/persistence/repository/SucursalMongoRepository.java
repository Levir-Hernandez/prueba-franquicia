package com.levir.hernandez.sucursal.infrastructure.adapter.out.persistence.repository;

import com.levir.hernandez.sucursal.infrastructure.adapter.out.persistence.document.SucursalDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface SucursalMongoRepository extends ReactiveMongoRepository<SucursalDocument, String>
{
    Flux<SucursalDocument> findByFranquiciaId(String franquiciaId);
}
