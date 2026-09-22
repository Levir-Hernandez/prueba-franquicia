package com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence;

import com.levir.hernandez.franquicia.application.port.out.FranquiciaRepositoryPort;
import com.levir.hernandez.franquicia.domain.model.Franquicia;
import com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.entity.FranquiciaEntity;
import com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.mapper.EntityMapper;
import com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.repository.FranquiciaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FranquiciaRepositoryAdapter implements FranquiciaRepositoryPort
{
    private final FranquiciaJpaRepository franquiciaRepository;

    @Override
    public Franquicia guardarFranquicia(Franquicia franquicia)
    {
        // Recupera la entidad existente para actualizarla o crea una nueva si no existe
        FranquiciaEntity franquiciaEntity = Optional.ofNullable(franquicia.getId())
                .flatMap(franquiciaRepository::findById)
                .orElseGet(FranquiciaEntity::new);

        franquiciaEntity.setNombre(franquicia.getNombre());

        return EntityMapper.toDomain(franquiciaRepository.save(franquiciaEntity));
    }

    @Override
    public Optional<Franquicia> obtenerFranquiciaPorId(UUID franquiciaId)
    {
        return franquiciaRepository.findById(franquiciaId)
                .map(EntityMapper::toDomain);
    }

    @Override
    public List<Franquicia> obtenerTodasLasFranquicias()
    {
        return franquiciaRepository.findAll().stream()
                .map(EntityMapper::toDomain)
                .toList();
    }
}
