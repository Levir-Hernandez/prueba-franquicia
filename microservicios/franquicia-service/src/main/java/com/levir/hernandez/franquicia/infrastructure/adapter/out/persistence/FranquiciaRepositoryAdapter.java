package com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence;

import com.levir.hernandez.franquicia.application.port.out.FranquiciaRepositoryPort;
import com.levir.hernandez.franquicia.domain.model.Franquicia;
import com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.document.FranquiciaDocument;
import com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.repository.FranquiciaMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FranquiciaRepositoryAdapter implements FranquiciaRepositoryPort
{
    private final FranquiciaMongoRepository franquiciaRepository;

    @Override
    public Mono<Franquicia> guardarFranquicia(Franquicia franquicia)
    {
        // Mongo no genera UUIDs: si la franquicia es nueva se le asigna uno aqui
        UUID id = Optional.ofNullable(franquicia.getId()).orElseGet(UUID::randomUUID);

        return franquiciaRepository.save(new FranquiciaDocument(id.toString(), franquicia.getNombre()))
                .map(FranquiciaRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<Franquicia> obtenerFranquiciaPorId(UUID franquiciaId)
    {
        return franquiciaRepository.findById(franquiciaId.toString())
                .map(FranquiciaRepositoryAdapter::toDomain);
    }

    @Override
    public Flux<Franquicia> obtenerTodasLasFranquicias()
    {
        return franquiciaRepository.findAll()
                .map(FranquiciaRepositoryAdapter::toDomain);
    }

    private static Franquicia toDomain(FranquiciaDocument document)
    {
        return new Franquicia(UUID.fromString(document.getId()), document.getNombre());
    }
}
