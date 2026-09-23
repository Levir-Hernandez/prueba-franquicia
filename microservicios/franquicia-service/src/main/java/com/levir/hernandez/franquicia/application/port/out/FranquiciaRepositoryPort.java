package com.levir.hernandez.franquicia.application.port.out;

import com.levir.hernandez.franquicia.domain.model.Franquicia;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FranquiciaRepositoryPort
{
    Mono<Franquicia> guardarFranquicia(Franquicia franquicia);
    /** Vacio si la franquicia no existe. */
    Mono<Franquicia> obtenerFranquiciaPorId(UUID franquiciaId);

    Flux<Franquicia> obtenerTodasLasFranquicias();
}
