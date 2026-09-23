package com.levir.hernandez.franquicia.application.port.in.franquicia;

import com.levir.hernandez.franquicia.domain.model.Franquicia;
import reactor.core.publisher.Flux;

public interface ObtenerFranquiciasUseCase
{
    Flux<Franquicia> obtenerFranquicias();
}
