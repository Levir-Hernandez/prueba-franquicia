package com.levir.hernandez.sucursal.application.service;

import com.levir.hernandez.sucursal.application.exception.FranquiciaNoEncontradaException;
import com.levir.hernandez.sucursal.application.exception.ServicioNoDisponibleException;
import com.levir.hernandez.sucursal.application.port.out.FranquiciaConsultaPort;
import com.levir.hernandez.sucursal.application.port.out.SucursalRepositoryPort;
import com.levir.hernandez.sucursal.domain.exception.SucursalInvalidaException;
import com.levir.hernandez.sucursal.domain.model.Sucursal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgregarSucursalServiceTest
{
    @Mock
    private SucursalRepositoryPort sucursalRepository;

    @Mock
    private FranquiciaConsultaPort franquiciaConsulta;

    @InjectMocks
    private AgregarSucursalService service;

    private final UUID franquiciaId = UUID.randomUUID();

    @Test
    @DisplayName("Deberia agregar una sucursal nueva asociada a su franquicia")
    void agregaSucursal()
    {
        when(franquiciaConsulta.existeFranquicia(franquiciaId)).thenReturn(true);
        when(sucursalRepository.guardarSucursal(any())).thenAnswer(inv -> inv.getArgument(0));

        Sucursal creada = service.agregarSucursal(franquiciaId, "Burger Express Centro");

        ArgumentCaptor<Sucursal> captor = ArgumentCaptor.forClass(Sucursal.class);
        verify(sucursalRepository).guardarSucursal(captor.capture());
        assertThat(captor.getValue().getId()).isNull();
        assertThat(creada.getNombre()).isEqualTo("Burger Express Centro");
        assertThat(creada.getFranquiciaId()).isEqualTo(franquiciaId);
    }

    @Test
    @DisplayName("Deberia lanzar excepcion y no guardar al agregar una sucursal con nombre invalido")
    void noAgregaSucursalConNombreInvalido()
    {
        assertThatThrownBy(() -> service.agregarSucursal(franquiciaId, ""))
                .isInstanceOf(SucursalInvalidaException.class);
        verifyNoInteractions(franquiciaConsulta);
        verify(sucursalRepository, never()).guardarSucursal(any());
    }

    @Test
    @DisplayName("Deberia lanzar excepcion y no guardar al agregar una sucursal a una franquicia que no existe")
    void noAgregaSucursalAFranquiciaInexistente()
    {
        when(franquiciaConsulta.existeFranquicia(franquiciaId)).thenReturn(false);

        assertThatThrownBy(() -> service.agregarSucursal(franquiciaId, "Burger Express Centro"))
                .isInstanceOf(FranquiciaNoEncontradaException.class);
        verify(sucursalRepository, never()).guardarSucursal(any());
    }

    @Test
    @DisplayName("Deberia propagar la excepcion y no guardar si el servicio de franquicias no esta disponible")
    void noAgregaSucursalSiServicioNoDisponible()
    {
        when(franquiciaConsulta.existeFranquicia(franquiciaId))
                .thenThrow(new ServicioNoDisponibleException("franquicias"));

        assertThatThrownBy(() -> service.agregarSucursal(franquiciaId, "Burger Express Centro"))
                .isInstanceOf(ServicioNoDisponibleException.class);
        verify(sucursalRepository, never()).guardarSucursal(any());
    }
}
