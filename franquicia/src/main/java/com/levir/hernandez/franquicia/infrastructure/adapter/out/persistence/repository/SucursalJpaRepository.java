package com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.repository;

import com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.entity.SucursalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SucursalJpaRepository extends JpaRepository<SucursalEntity, UUID>
{
    List<SucursalEntity> findByFranquiciaId(UUID franquiciaId);
}
