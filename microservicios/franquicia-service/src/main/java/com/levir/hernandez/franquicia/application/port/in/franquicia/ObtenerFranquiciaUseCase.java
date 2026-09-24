package com.levir.hernandez.franquicia.application.port.in.franquicia;

import com.levir.hernandez.franquicia.domain.model.Franquicia;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ObtenerFranquiciaUseCase
{
    Mono<Franquicia> obtenerFranquicia(UUID franquiciaId);
}
