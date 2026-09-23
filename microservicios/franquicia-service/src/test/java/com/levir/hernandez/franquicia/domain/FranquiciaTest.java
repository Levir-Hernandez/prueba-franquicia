package com.levir.hernandez.franquicia.domain;

import com.levir.hernandez.franquicia.domain.exception.FranquiciaInvalidaException;
import com.levir.hernandez.franquicia.domain.model.Franquicia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FranquiciaTest
{
    @Test
    @DisplayName("Deberia crear una franquicia con nombre valido")
    void creaFranquiciaConNombreValido()
    {
        Franquicia franquicia = new Franquicia("Burger Express");

        assertThat(franquicia.getNombre()).isEqualTo("Burger Express");
    }

    @ParameterizedTest
    @DisplayName("Deberia lanzar excepcion al crear una franquicia con nombre nulo o vacio")
    @NullAndEmptySource
    void noCreaFranquiciaConNombreInvalido(String nombre)
    {
        assertThatThrownBy(() -> new Franquicia(nombre)).isInstanceOf(FranquiciaInvalidaException.class);
    }

    @Test
    @DisplayName("Deberia renombrar una franquicia con nombre valido")
    void renombraConNombreValido()
    {
        Franquicia franquicia = new Franquicia("Burger Express");

        franquicia.renombrar("Pizza Rapida");

        assertThat(franquicia.getNombre()).isEqualTo("Pizza Rapida");
    }

    @ParameterizedTest
    @DisplayName("Deberia lanzar excepcion al renombrar una franquicia con nombre nulo o vacio")
    @NullAndEmptySource
    void noRenombraConNombreInvalido(String nombre)
    {
        Franquicia franquicia = new Franquicia("Burger Express");

        assertThatThrownBy(() -> franquicia.renombrar(nombre)).isInstanceOf(FranquiciaInvalidaException.class);
    }
}
