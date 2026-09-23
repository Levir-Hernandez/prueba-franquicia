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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        when(franquiciaRepository.guardarFranquicia(any())).thenAnswer(inv -> inv.getArgument(0));

        Franquicia creada = service.agregarFranquicia("Burger Express");

        ArgumentCaptor<Franquicia> captor = ArgumentCaptor.forClass(Franquicia.class);
        verify(franquiciaRepository).guardarFranquicia(captor.capture());
        assertThat(captor.getValue().getId()).isNull();
        assertThat(creada.getNombre()).isEqualTo("Burger Express");
    }

    @Test
    @DisplayName("Deberia lanzar excepcion y no guardar al agregar una franquicia con nombre invalido")
    void noAgregaFranquiciaConNombreInvalido()
    {
        assertThatThrownBy(() -> service.agregarFranquicia("")).isInstanceOf(FranquiciaInvalidaException.class);
        verify(franquiciaRepository, never()).guardarFranquicia(any());
    }

    @Test
    @DisplayName("Deberia obtener una franquicia existente por su id")
    void obtieneFranquiciaExistente()
    {
        when(franquiciaRepository.obtenerFranquiciaPorId(franquiciaId))
                .thenReturn(Optional.of(new Franquicia(franquiciaId, "Burger Express")));

        assertThat(service.obtenerFranquicia(franquiciaId).getNombre()).isEqualTo("Burger Express");
    }

    @Test
    @DisplayName("Deberia lanzar excepcion al obtener una franquicia que no existe")
    void noObtieneFranquiciaInexistente()
    {
        when(franquiciaRepository.obtenerFranquiciaPorId(franquiciaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerFranquicia(franquiciaId))
                .isInstanceOf(FranquiciaNoEncontradaException.class);
    }

    @Test
    @DisplayName("Deberia obtener todas las franquicias cuando hay franquicias")
    void obtieneFranquicias()
    {
        when(franquiciaRepository.obtenerTodasLasFranquicias()).thenReturn(List.of(
                new Franquicia(UUID.randomUUID(), "Burger Express"),
                new Franquicia(UUID.randomUUID(), "Pizza Rapida")));

        assertThat(service.obtenerFranquicias()).extracting(Franquicia::getNombre)
                .containsExactlyInAnyOrder("Burger Express", "Pizza Rapida");
    }

    @Test
    @DisplayName("Deberia devolver una lista vacia cuando no hay franquicias")
    void obtieneListaVaciaSinFranquicias()
    {
        when(franquiciaRepository.obtenerTodasLasFranquicias()).thenReturn(List.of());

        assertThat(service.obtenerFranquicias()).isEmpty();
    }

    @Test
    @DisplayName("Deberia renombrar una franquicia existente")
    void renombraFranquicia()
    {
        when(franquiciaRepository.obtenerFranquiciaPorId(franquiciaId))
                .thenReturn(Optional.of(new Franquicia(franquiciaId, "Burger Express")));
        when(franquiciaRepository.guardarFranquicia(any())).thenAnswer(inv -> inv.getArgument(0));

        Franquicia renombrada = service.renombrarFranquicia(franquiciaId, "Pizza Rapida");

        assertThat(renombrada.getId()).isEqualTo(franquiciaId);
        assertThat(renombrada.getNombre()).isEqualTo("Pizza Rapida");
    }

    @Test
    @DisplayName("Deberia lanzar excepcion y no guardar al renombrar una franquicia que no existe")
    void noRenombraFranquiciaInexistente()
    {
        when(franquiciaRepository.obtenerFranquiciaPorId(franquiciaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.renombrarFranquicia(franquiciaId, "Pizza Rapida"))
                .isInstanceOf(FranquiciaNoEncontradaException.class);
        verify(franquiciaRepository, never()).guardarFranquicia(any());
    }

    @Test
    @DisplayName("Deberia lanzar excepcion y no guardar al renombrar una franquicia con nombre invalido")
    void noRenombraFranquiciaConNombreInvalido()
    {
        when(franquiciaRepository.obtenerFranquiciaPorId(franquiciaId))
                .thenReturn(Optional.of(new Franquicia(franquiciaId, "Burger Express")));

        assertThatThrownBy(() -> service.renombrarFranquicia(franquiciaId, ""))
                .isInstanceOf(FranquiciaInvalidaException.class);
        verify(franquiciaRepository, never()).guardarFranquicia(any());
    }
}
