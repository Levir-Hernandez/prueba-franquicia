package com.levir.hernandez.franquicia.application.service;

import com.levir.hernandez.franquicia.application.annotation.ObservableUseCase;
import com.levir.hernandez.franquicia.application.exception.FranquiciaNoEncontradaException;
import com.levir.hernandez.franquicia.application.port.in.franquicia.*;
import com.levir.hernandez.franquicia.application.port.out.FranquiciaRepositoryPort;
import com.levir.hernandez.franquicia.domain.model.Franquicia;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@ObservableUseCase
@RequiredArgsConstructor
public class FranquiciaService implements
        ObtenerFranquiciaUseCase, ObtenerFranquiciasUseCase,
        RenombrarFranquiciaUseCase, AgregarFranquiciaUseCase
{
    private final FranquiciaRepositoryPort franquiciaRepositoryPort;

    @Override
    public Franquicia agregarFranquicia(String nombre)
    {
        return franquiciaRepositoryPort.guardarFranquicia(new Franquicia(null, nombre));
    }

    @Override
    public Franquicia obtenerFranquicia(UUID franquiciaId)
    {
        return franquiciaRepositoryPort.obtenerFranquiciaPorId(franquiciaId)
                .orElseThrow(() -> new FranquiciaNoEncontradaException(franquiciaId));
    }

    @Override
    public List<Franquicia> obtenerFranquicias()
    {
        return franquiciaRepositoryPort.obtenerTodasLasFranquicias();
    }

    @Override
    public Franquicia renombrarFranquicia(UUID franquiciaId, String nombre)
    {
        Franquicia franquicia = obtenerFranquicia(franquiciaId);
        franquicia.renombrar(nombre);
        return franquiciaRepositoryPort.guardarFranquicia(franquicia);
    }
}
