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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
                .thenReturn(Mono.just(new Sucursal(sucursalId, "Burger Express Centro", franquiciaId)));

        StepVerifier.create(service.obtenerSucursal(sucursalId).map(Sucursal::getNombre))
                .expectNext("Burger Express Centro")
                .verifyComplete();
    }

    @Test
    @DisplayName("Deberia emitir error al obtener una sucursal que no existe")
    void noObtieneSucursalInexistente()
    {
        when(sucursalRepository.obtenerSucursalPorId(sucursalId)).thenReturn(Mono.empty());

        StepVerifier.create(service.obtenerSucursal(sucursalId))
                .expectError(SucursalNoEncontradaException.class)
                .verify();
    }

    @Test
    @DisplayName("Deberia obtener las sucursales de una franquicia que tiene sucursales")
    void obtieneSucursalesDeFranquicia()
    {
        when(sucursalRepository.obtenerSucursalesPorIdDeFranquicia(franquiciaId)).thenReturn(Flux.just(
                new Sucursal(UUID.randomUUID(), "Burger Express Centro", franquiciaId),
                new Sucursal(UUID.randomUUID(), "Burger Express Poblado", franquiciaId)));

        StepVerifier.create(service.obtenerSucursales(franquiciaId).map(Sucursal::getNombre))
                .expectNext("Burger Express Centro", "Burger Express Poblado")
                .verifyComplete();
    }

    @Test
    @DisplayName("Deberia terminar vacio para una franquicia sin sucursales o que no existe")
    void obtieneListaVaciaSinSucursales()
    {
        when(sucursalRepository.obtenerSucursalesPorIdDeFranquicia(franquiciaId)).thenReturn(Flux.empty());

        StepVerifier.create(service.obtenerSucursales(franquiciaId)).verifyComplete();
    }

    @Test
    @DisplayName("Deberia renombrar una sucursal existente")
    void renombraSucursal()
    {
        when(sucursalRepository.obtenerSucursalPorId(sucursalId))
                .thenReturn(Mono.just(new Sucursal(sucursalId, "Burger Express Centro", franquiciaId)));
        when(sucursalRepository.guardarSucursal(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.renombrarSucursal(sucursalId, "Burger Express Poblado"))
                .assertNext(renombrada ->
                {
                    assertThat(renombrada.getId()).isEqualTo(sucursalId);
                    assertThat(renombrada.getNombre()).isEqualTo("Burger Express Poblado");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Deberia emitir error y no guardar al renombrar una sucursal que no existe")
    void noRenombraSucursalInexistente()
    {
        when(sucursalRepository.obtenerSucursalPorId(sucursalId)).thenReturn(Mono.empty());

        StepVerifier.create(service.renombrarSucursal(sucursalId, "Burger Express Poblado"))
                .expectError(SucursalNoEncontradaException.class)
                .verify();

        verify(sucursalRepository, never()).guardarSucursal(any());
    }

    @Test
    @DisplayName("Deberia emitir error y no guardar al renombrar una sucursal con nombre invalido")
    void noRenombraSucursalConNombreInvalido()
    {
        when(sucursalRepository.obtenerSucursalPorId(sucursalId))
                .thenReturn(Mono.just(new Sucursal(sucursalId, "Burger Express Centro", franquiciaId)));

        StepVerifier.create(service.renombrarSucursal(sucursalId, ""))
                .expectError(SucursalInvalidaException.class)
                .verify();

        verify(sucursalRepository, never()).guardarSucursal(any());
    }
}
