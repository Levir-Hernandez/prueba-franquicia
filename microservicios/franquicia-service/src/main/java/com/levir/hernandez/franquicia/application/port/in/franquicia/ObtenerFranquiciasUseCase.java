package com.levir.hernandez.franquicia.application.port.in.franquicia;

import com.levir.hernandez.franquicia.domain.model.Franquicia;

import java.util.List;

public interface ObtenerFranquiciasUseCase
{
    List<Franquicia> obtenerFranquicias();
}
