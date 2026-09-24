package com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.repository;

import com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.document.FranquiciaDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface FranquiciaMongoRepository extends ReactiveMongoRepository<FranquiciaDocument, String>
{
}
