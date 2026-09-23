package com.levir.hernandez.franquicia.application.port.in.franquicia;

import com.levir.hernandez.franquicia.domain.model.Franquicia;

import java.util.UUID;

public interface RenombrarFranquiciaUseCase
{
    Franquicia renombrarFranquicia(UUID franquiciaId, String nombre);
}
