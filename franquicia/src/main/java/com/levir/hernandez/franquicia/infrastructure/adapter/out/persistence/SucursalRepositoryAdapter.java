package com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence;

import com.levir.hernandez.franquicia.application.port.out.SucursalRepositoryPort;
import com.levir.hernandez.franquicia.domain.model.Sucursal;
import com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.entity.FranquiciaEntity;
import com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.entity.SucursalEntity;
import com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.mapper.EntityMapper;
import com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.repository.SucursalJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SucursalRepositoryAdapter implements SucursalRepositoryPort
{
    private final SucursalJpaRepository sucursalRepository;

    @Override
    public Sucursal guardarSucursal(Sucursal sucursal)
    {
        // Recupera la entidad existente para actualizarla o crea una nueva si no existe
        SucursalEntity entity = Optional.ofNullable(sucursal.getId())
                .flatMap(sucursalRepository::findById)
                .orElseGet(SucursalEntity::new);

        entity.setNombre(sucursal.getNombre());
        entity.setFranquicia(new FranquiciaEntity(sucursal.getFranquiciaId()));

        return EntityMapper.toDomain(sucursalRepository.save(entity));
    }

    @Override
    public Optional<Sucursal> obtenerSucursalPorId(UUID sucursalId)
    {
        return sucursalRepository.findById(sucursalId)
                .map(EntityMapper::toDomain);
    }

    @Override
    public List<Sucursal> obtenerSucursalesPorIdDeFranquicia(UUID franquiciaId)
    {
        return sucursalRepository.findByFranquiciaId(franquiciaId).stream()
                .map(EntityMapper::toDomain)
                .toList();
    }
}
