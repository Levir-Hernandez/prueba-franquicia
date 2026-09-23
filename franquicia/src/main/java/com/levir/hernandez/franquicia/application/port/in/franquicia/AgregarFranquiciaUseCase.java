package com.levir.hernandez.franquicia.application.port.in.franquicia;

import com.levir.hernandez.franquicia.domain.model.Franquicia;

public interface AgregarFranquiciaUseCase
{
    Franquicia agregarFranquicia(String nombre);
}
