package com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.repository;

import com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.document.FranquiciaDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FranquiciaMongoRepository extends MongoRepository<FranquiciaDocument, String>
{
}
