package com.levir.hernandez.franquicia.application.port.out;

import com.levir.hernandez.franquicia.domain.model.Franquicia;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FranquiciaRepositoryPort
{
    Franquicia guardarFranquicia(Franquicia franquicia);
    Optional<Franquicia> obtenerFranquiciaPorId(UUID franquiciaId);

    List<Franquicia> obtenerTodasLasFranquicias();
}
