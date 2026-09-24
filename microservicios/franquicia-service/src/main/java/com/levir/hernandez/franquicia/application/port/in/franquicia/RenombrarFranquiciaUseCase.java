package com.levir.hernandez.franquicia.application.port.in.franquicia;

import com.levir.hernandez.franquicia.domain.model.Franquicia;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RenombrarFranquiciaUseCase
{
    Mono<Franquicia> renombrarFranquicia(UUID franquiciaId, String nombre);
}
