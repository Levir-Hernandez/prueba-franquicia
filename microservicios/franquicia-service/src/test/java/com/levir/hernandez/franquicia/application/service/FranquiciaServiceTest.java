package com.levir.hernandez.franquicia.application.service;

import com.levir.hernandez.franquicia.application.exception.FranquiciaNoEncontradaException;
import com.levir.hernandez.franquicia.application.port.out.FranquiciaRepositoryPort;
import com.levir.hernandez.franquicia.domain.exception.FranquiciaInvalidaException;
import com.levir.hernandez.franquicia.domain.model.Franquicia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class FranquiciaServiceTest
{
    @Mock
    private FranquiciaRepositoryPort franquiciaRepository;

    @InjectMocks
    private FranquiciaService service;

    private final UUID franquiciaId = UUID.randomUUID();

    @Test
    @DisplayName("Deberia agregar una franquicia nueva")
    void agregaFranquicia()
    {
        when(franquiciaRepository.guardarFranquicia(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.agregarFranquicia("Burger Express").map(Franquicia::getNombre))
                .expectNext("Burger Express")
                .verifyComplete();

        ArgumentCaptor<Franquicia> captor = ArgumentCaptor.forClass(Franquicia.class);
        verify(franquiciaRepository).guardarFranquicia(captor.capture());
        assertThat(captor.getValue().getId()).isNull();
    }

    @Test
    @DisplayName("Deberia emitir error y no guardar al agregar una franquicia con nombre invalido")
    void noAgregaFranquiciaConNombreInvalido()
    {
        StepVerifier.create(service.agregarFranquicia(""))
                .expectError(FranquiciaInvalidaException.class)
                .verify();

        verify(franquiciaRepository, never()).guardarFranquicia(any());
    }

    @Test
    @DisplayName("Deberia obtener una franquicia existente por su id")
    void obtieneFranquiciaExistente()
    {
        when(franquiciaRepository.obtenerFranquiciaPorId(franquiciaId))
                .thenReturn(Mono.just(new Franquicia(franquiciaId, "Burger Express")));

        StepVerifier.create(service.obtenerFranquicia(franquiciaId).map(Franquicia::getNombre))
                .expectNext("Burger Express")
                .verifyComplete();
    }

    @Test
    @DisplayName("Deberia emitir error al obtener una franquicia que no existe")
    void noObtieneFranquiciaInexistente()
    {
        when(franquiciaRepository.obtenerFranquiciaPorId(franquiciaId)).thenReturn(Mono.empty());

        StepVerifier.create(service.obtenerFranquicia(franquiciaId))
                .expectError(FranquiciaNoEncontradaException.class)
                .verify();
    }

    @Test
    @DisplayName("Deberia obtener todas las franquicias cuando hay franquicias")
    void obtieneFranquicias()
    {
        when(franquiciaRepository.obtenerTodasLasFranquicias()).thenReturn(Flux.just(
                new Franquicia(UUID.randomUUID(), "Burger Express"),
                new Franquicia(UUID.randomUUID(), "Pizza Rapida")));

        StepVerifier.create(service.obtenerFranquicias().map(Franquicia::getNombre))
                .expectNext("Burger Express", "Pizza Rapida")
                .verifyComplete();
    }

    @Test
    @DisplayName("Deberia terminar vacio cuando no hay franquicias")
    void obtieneListaVaciaSinFranquicias()
    {
        when(franquiciaRepository.obtenerTodasLasFranquicias()).thenReturn(Flux.empty());

        StepVerifier.create(service.obtenerFranquicias()).verifyComplete();
    }

    @Test
    @DisplayName("Deberia renombrar una franquicia existente")
    void renombraFranquicia()
    {
        when(franquiciaRepository.obtenerFranquiciaPorId(franquiciaId))
                .thenReturn(Mono.just(new Franquicia(franquiciaId, "Burger Express")));
        when(franquiciaRepository.guardarFranquicia(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.renombrarFranquicia(franquiciaId, "Pizza Rapida"))
                .assertNext(renombrada ->
                {
                    assertThat(renombrada.getId()).isEqualTo(franquiciaId);
                    assertThat(renombrada.getNombre()).isEqualTo("Pizza Rapida");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Deberia emitir error y no guardar al renombrar una franquicia que no existe")
    void noRenombraFranquiciaInexistente()
    {
        when(franquiciaRepository.obtenerFranquiciaPorId(franquiciaId)).thenReturn(Mono.empty());

        StepVerifier.create(service.renombrarFranquicia(franquiciaId, "Pizza Rapida"))
                .expectError(FranquiciaNoEncontradaException.class)
                .verify();

        verify(franquiciaRepository, never()).guardarFranquicia(any());
    }

    @Test
    @DisplayName("Deberia emitir error y no guardar al renombrar una franquicia con nombre invalido")
    void noRenombraFranquiciaConNombreInvalido()
    {
        when(franquiciaRepository.obtenerFranquiciaPorId(franquiciaId))
                .thenReturn(Mono.just(new Franquicia(franquiciaId, "Burger Express")));

        StepVerifier.create(service.renombrarFranquicia(franquiciaId, ""))
                .expectError(FranquiciaInvalidaException.class)
                .verify();

        verify(franquiciaRepository, never()).guardarFranquicia(any());
    }
}
