package com.levir.hernandez.sucursal.infrastructure.adapter.out.persistence;

import com.levir.hernandez.sucursal.application.port.out.SucursalRepositoryPort;
import com.levir.hernandez.sucursal.domain.model.Sucursal;
import com.levir.hernandez.sucursal.infrastructure.adapter.out.persistence.document.SucursalDocument;
import com.levir.hernandez.sucursal.infrastructure.adapter.out.persistence.repository.SucursalMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SucursalRepositoryAdapter implements SucursalRepositoryPort
{
    private final SucursalMongoRepository sucursalRepository;

    @Override
    public Mono<Sucursal> guardarSucursal(Sucursal sucursal)
    {
        // Mongo no genera UUIDs: si la sucursal es nueva se le asigna uno aqui
        UUID id = Optional.ofNullable(sucursal.getId()).orElseGet(UUID::randomUUID);

        return sucursalRepository.save(new SucursalDocument(
                        id.toString(), sucursal.getNombre(), sucursal.getFranquiciaId().toString()))
                .map(SucursalRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<Sucursal> obtenerSucursalPorId(UUID sucursalId)
    {
        return sucursalRepository.findById(sucursalId.toString())
                .map(SucursalRepositoryAdapter::toDomain);
    }

    @Override
    public Flux<Sucursal> obtenerSucursalesPorIdDeFranquicia(UUID franquiciaId)
    {
        return sucursalRepository.findByFranquiciaId(franquiciaId.toString())
                .map(SucursalRepositoryAdapter::toDomain);
    }

    private static Sucursal toDomain(SucursalDocument document)
    {
        return new Sucursal(UUID.fromString(document.getId()), document.getNombre(),
                UUID.fromString(document.getFranquiciaId()));
    }
}
