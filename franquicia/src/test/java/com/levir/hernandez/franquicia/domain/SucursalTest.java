package com.levir.hernandez.franquicia.domain;

import com.levir.hernandez.franquicia.domain.exception.SucursalInvalidaException;
import com.levir.hernandez.franquicia.domain.model.Sucursal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SucursalTest
{
    private final UUID franquiciaId = UUID.randomUUID();

    @Test
    @DisplayName("Deberia crear una sucursal con nombre valido")
    void creaSucursalConNombreValido()
    {
        Sucursal sucursal = new Sucursal(null, "Burger Express Centro", franquiciaId);

        assertThat(sucursal.getNombre()).isEqualTo("Burger Express Centro");
    }

    @ParameterizedTest
    @DisplayName("Deberia lanzar excepcion al crear una sucursal con nombre nulo o vacio")
    @NullAndEmptySource
    void noCreaSucursalConNombreInvalido(String nombre)
    {
        assertThatThrownBy(() -> new Sucursal(null, nombre, franquiciaId))
                .isInstanceOf(SucursalInvalidaException.class);
    }

    @Test
    @DisplayName("Deberia renombrar una sucursal con nombre valido")
    void renombraConNombreValido()
    {
        Sucursal sucursal = new Sucursal(null, "Burger Express Centro", franquiciaId);

        sucursal.renombrar("Burger Express Poblado");

        assertThat(sucursal.getNombre()).isEqualTo("Burger Express Poblado");
    }

    @ParameterizedTest
    @DisplayName("Deberia lanzar excepcion al renombrar una sucursal con nombre nulo o vacio")
    @NullAndEmptySource
    void noRenombraConNombreInvalido(String nombre)
    {
        Sucursal sucursal = new Sucursal(null, "Burger Express Centro", franquiciaId);

        assertThatThrownBy(() -> sucursal.renombrar(nombre)).isInstanceOf(SucursalInvalidaException.class);
    }
}
