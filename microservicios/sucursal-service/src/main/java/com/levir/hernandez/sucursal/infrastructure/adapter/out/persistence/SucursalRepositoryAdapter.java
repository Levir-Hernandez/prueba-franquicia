package com.levir.hernandez.sucursal.infrastructure.adapter.out.persistence;

import com.levir.hernandez.sucursal.application.port.out.SucursalRepositoryPort;
import com.levir.hernandez.sucursal.domain.model.Sucursal;
import com.levir.hernandez.sucursal.infrastructure.adapter.out.persistence.document.SucursalDocument;
import com.levir.hernandez.sucursal.infrastructure.adapter.out.persistence.repository.SucursalMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SucursalRepositoryAdapter implements SucursalRepositoryPort
{
    private final SucursalMongoRepository sucursalRepository;

    @Override
    public Sucursal guardarSucursal(Sucursal sucursal)
    {
        // Mongo no genera UUIDs: si la sucursal es nueva se le asigna uno aqui
        UUID id = Optional.ofNullable(sucursal.getId()).orElseGet(UUID::randomUUID);

        return toDomain(sucursalRepository.save(new SucursalDocument(
                id.toString(), sucursal.getNombre(), sucursal.getFranquiciaId().toString())));
    }

    @Override
    public Optional<Sucursal> obtenerSucursalPorId(UUID sucursalId)
    {
        return sucursalRepository.findById(sucursalId.toString())
                .map(SucursalRepositoryAdapter::toDomain);
    }

    @Override
    public List<Sucursal> obtenerSucursalesPorIdDeFranquicia(UUID franquiciaId)
    {
        return sucursalRepository.findByFranquiciaId(franquiciaId.toString()).stream()
                .map(SucursalRepositoryAdapter::toDomain)
                .toList();
    }

    private static Sucursal toDomain(SucursalDocument document)
    {
        return new Sucursal(UUID.fromString(document.getId()), document.getNombre(),
                UUID.fromString(document.getFranquiciaId()));
    }
}
