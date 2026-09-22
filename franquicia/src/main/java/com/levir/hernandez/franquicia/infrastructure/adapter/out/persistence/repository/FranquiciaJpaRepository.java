package com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.repository;

import com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.entity.FranquiciaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FranquiciaJpaRepository extends JpaRepository<FranquiciaEntity, UUID>
{
    List<FranquiciaEntity> findAll();
}
