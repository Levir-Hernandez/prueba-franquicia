package com.levir.hernandez.sucursal.application.service;

import com.levir.hernandez.sucursal.application.exception.SucursalNoEncontradaException;
import com.levir.hernandez.sucursal.application.port.out.SucursalRepositoryPort;
import com.levir.hernandez.sucursal.domain.exception.SucursalInvalidaException;
import com.levir.hernandez.sucursal.domain.model.Sucursal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SucursalServiceTest
{
    @Mock
    private SucursalRepositoryPort sucursalRepository;

    @InjectMocks
    private SucursalService service;

    private final UUID franquiciaId = UUID.randomUUID();
    private final UUID sucursalId = UUID.randomUUID();

    @Test
    @DisplayName("Deberia obtener una sucursal existente por su id")
    void obtieneSucursalExistente()
    {
        when(sucursalRepository.obtenerSucursalPorId(sucursalId))
                .thenReturn(Optional.of(new Sucursal(sucursalId, "Burger Express Centro", franquiciaId)));

        assertThat(service.obtenerSucursal(sucursalId).getNombre()).isEqualTo("Burger Express Centro");
    }

    @Test
    @DisplayName("Deberia lanzar excepcion al obtener una sucursal que no existe")
    void noObtieneSucursalInexistente()
    {
        when(sucursalRepository.obtenerSucursalPorId(sucursalId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerSucursal(sucursalId))
                .isInstanceOf(SucursalNoEncontradaException.class);
    }

    @Test
    @DisplayName("Deberia obtener las sucursales de una franquicia que tiene sucursales")
    void obtieneSucursalesDeFranquicia()
    {
        when(sucursalRepository.obtenerSucursalesPorIdDeFranquicia(franquiciaId)).thenReturn(List.of(
                new Sucursal(UUID.randomUUID(), "Burger Express Centro", franquiciaId),
                new Sucursal(UUID.randomUUID(), "Burger Express Poblado", franquiciaId)));

        assertThat(service.obtenerSucursales(franquiciaId)).extracting(Sucursal::getNombre)
                .containsExactlyInAnyOrder("Burger Express Centro", "Burger Express Poblado");
    }

    @Test
    @DisplayName("Deberia devolver una lista vacia para una franquicia sin sucursales o que no existe")
    void obtieneListaVaciaSinSucursales()
    {
        when(sucursalRepository.obtenerSucursalesPorIdDeFranquicia(franquiciaId)).thenReturn(List.of());

        assertThat(service.obtenerSucursales(franquiciaId)).isEmpty();
    }

    @Test
    @DisplayName("Deberia renombrar una sucursal existente")
    void renombraSucursal()
    {
        when(sucursalRepository.obtenerSucursalPorId(sucursalId))
                .thenReturn(Optional.of(new Sucursal(sucursalId, "Burger Express Centro", franquiciaId)));
        when(sucursalRepository.guardarSucursal(any())).thenAnswer(inv -> inv.getArgument(0));

        Sucursal renombrada = service.renombrarSucursal(sucursalId, "Burger Express Poblado");

        assertThat(renombrada.getId()).isEqualTo(sucursalId);
        assertThat(renombrada.getNombre()).isEqualTo("Burger Express Poblado");
    }

    @Test
    @DisplayName("Deberia lanzar excepcion y no guardar al renombrar una sucursal que no existe")
    void noRenombraSucursalInexistente()
    {
        when(sucursalRepository.obtenerSucursalPorId(sucursalId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.renombrarSucursal(sucursalId, "Burger Express Poblado"))
                .isInstanceOf(SucursalNoEncontradaException.class);
        verify(sucursalRepository, never()).guardarSucursal(any());
    }

    @Test
    @DisplayName("Deberia lanzar excepcion y no guardar al renombrar una sucursal con nombre invalido")
    void noRenombraSucursalConNombreInvalido()
    {
        when(sucursalRepository.obtenerSucursalPorId(sucursalId))
                .thenReturn(Optional.of(new Sucursal(sucursalId, "Burger Express Centro", franquiciaId)));

        assertThatThrownBy(() -> service.renombrarSucursal(sucursalId, ""))
                .isInstanceOf(SucursalInvalidaException.class);
        verify(sucursalRepository, never()).guardarSucursal(any());
    }
}
