package com.levir.hernandez.franquicia.domain;

import com.levir.hernandez.franquicia.domain.exception.ProductoInvalidoException;
import com.levir.hernandez.franquicia.domain.model.Producto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductoTest
{
    private final Producto producto = new Producto(null, "Hamburguesa clasica", 10, UUID.randomUUID());

    @Test
    @DisplayName("Deberia renombrar un producto con nombre valido")
    void renombraConNombreValido()
    {
        producto.renombrar("Hamburguesa doble");

        assertThat(producto.getNombre()).isEqualTo("Hamburguesa doble");
    }

    @ParameterizedTest
    @DisplayName("Deberia lanzar excepcion al renombrar un producto con nombre nulo o vacio")
    @NullAndEmptySource
    void noRenombraConNombreInvalido(String nombre)
    {
        assertThatThrownBy(() -> producto.renombrar(nombre)).isInstanceOf(ProductoInvalidoException.class);
    }

    @ParameterizedTest
    @DisplayName("Deberia modificar el stock de un producto a cero o a un valor positivo")
    @ValueSource(ints = {0, 25})
    void modificaStockConCeroOPositivo(int stock)
    {
        producto.modificarStock(stock);

        assertThat(producto.getStock()).isEqualTo(stock);
    }

    @Test
    @DisplayName("Deberia lanzar excepcion al modificar el stock de un producto a un valor negativo")
    void noModificaStockNegativo()
    {
        assertThatThrownBy(() -> producto.modificarStock(-1)).isInstanceOf(ProductoInvalidoException.class);
    }
}
