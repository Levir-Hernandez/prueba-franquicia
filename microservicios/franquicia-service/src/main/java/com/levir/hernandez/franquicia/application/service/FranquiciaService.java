package com.levir.hernandez.franquicia.application.service;

import com.levir.hernandez.franquicia.application.annotation.ObservableUseCase;
import com.levir.hernandez.franquicia.application.exception.FranquiciaNoEncontradaException;
import com.levir.hernandez.franquicia.application.port.in.franquicia.*;
import com.levir.hernandez.franquicia.application.port.out.FranquiciaRepositoryPort;
import com.levir.hernandez.franquicia.domain.model.Franquicia;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@ObservableUseCase
@RequiredArgsConstructor
public class FranquiciaService implements
        ObtenerFranquiciaUseCase, ObtenerFranquiciasUseCase,
        RenombrarFranquiciaUseCase, AgregarFranquiciaUseCase
{
    private final FranquiciaRepositoryPort franquiciaRepositoryPort;

    @Override
    public Mono<Franquicia> agregarFranquicia(String nombre)
    {
        return Mono.fromCallable(() -> new Franquicia(null, nombre))
                .flatMap(franquiciaRepositoryPort::guardarFranquicia);
    }

    @Override
    public Mono<Franquicia> obtenerFranquicia(UUID franquiciaId)
    {
        return franquiciaRepositoryPort.obtenerFranquiciaPorId(franquiciaId)
                .switchIfEmpty(Mono.error(() -> new FranquiciaNoEncontradaException(franquiciaId)));
    }

    @Override
    public Flux<Franquicia> obtenerFranquicias()
    {
        return franquiciaRepositoryPort.obtenerTodasLasFranquicias();
    }

    @Override
    public Mono<Franquicia> renombrarFranquicia(UUID franquiciaId, String nombre)
    {
        return obtenerFranquicia(franquiciaId)
                .map(franquicia ->
                {
                    franquicia.renombrar(nombre);
                    return franquicia;
                })
                .flatMap(franquiciaRepositoryPort::guardarFranquicia);
    }
}
