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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
        when(franquiciaConsulta.existeFranquicia(franquiciaId)).thenReturn(Mono.just(true));
        when(sucursalRepository.guardarSucursal(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.agregarSucursal(franquiciaId, "Burger Express Centro"))
                .assertNext(creada ->
                {
                    assertThat(creada.getNombre()).isEqualTo("Burger Express Centro");
                    assertThat(creada.getFranquiciaId()).isEqualTo(franquiciaId);
                })
                .verifyComplete();

        ArgumentCaptor<Sucursal> captor = ArgumentCaptor.forClass(Sucursal.class);
        verify(sucursalRepository).guardarSucursal(captor.capture());
        assertThat(captor.getValue().getId()).isNull();
    }

    @Test
    @DisplayName("Deberia emitir error y no guardar al agregar una sucursal con nombre invalido")
    void noAgregaSucursalConNombreInvalido()
    {
        StepVerifier.create(service.agregarSucursal(franquiciaId, ""))
                .expectError(SucursalInvalidaException.class)
                .verify();

        verifyNoInteractions(franquiciaConsulta);
        verify(sucursalRepository, never()).guardarSucursal(any());
    }

    @Test
    @DisplayName("Deberia emitir error y no guardar al agregar una sucursal a una franquicia que no existe")
    void noAgregaSucursalAFranquiciaInexistente()
    {
        when(franquiciaConsulta.existeFranquicia(franquiciaId)).thenReturn(Mono.just(false));

        StepVerifier.create(service.agregarSucursal(franquiciaId, "Burger Express Centro"))
                .expectError(FranquiciaNoEncontradaException.class)
                .verify();

        verify(sucursalRepository, never()).guardarSucursal(any());
    }

    @Test
    @DisplayName("Deberia propagar el error y no guardar si el servicio de franquicias no esta disponible")
    void noAgregaSucursalSiServicioNoDisponible()
    {
        when(franquiciaConsulta.existeFranquicia(franquiciaId))
                .thenReturn(Mono.error(new ServicioNoDisponibleException("franquicias")));

        StepVerifier.create(service.agregarSucursal(franquiciaId, "Burger Express Centro"))
                .expectError(ServicioNoDisponibleException.class)
                .verify();

        verify(sucursalRepository, never()).guardarSucursal(any());
    }
}
