package com.levir.hernandez.sucursal.infrastructure.adapter.out.persistence.repository;

import com.levir.hernandez.sucursal.infrastructure.adapter.out.persistence.document.SucursalDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SucursalMongoRepository extends MongoRepository<SucursalDocument, String>
{
    List<SucursalDocument> findByFranquiciaId(String franquiciaId);
}
