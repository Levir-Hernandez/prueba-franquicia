package com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence;

import com.levir.hernandez.franquicia.domain.model.Franquicia;
import com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.entity.FranquiciaEntity;
import com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.repository.FranquiciaJpaRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FranquiciaRepositoryAdapterTest
{
    @Mock
    private FranquiciaJpaRepository franquiciaRepository;

    @InjectMocks
    private FranquiciaRepositoryAdapter adapter;

    private final UUID franquiciaId = UUID.randomUUID();

    @Test
    @DisplayName("Deberia guardar una franquicia nueva")
    void guardaFranquiciaNueva()
    {
        when(franquiciaRepository.save(any())).thenAnswer(inv -> {
            FranquiciaEntity entity = inv.getArgument(0);
            entity.setId(franquiciaId);
            return entity;
        });

        Franquicia guardada = adapter.guardarFranquicia(new Franquicia("Burger Express"));

        assertThat(guardada.getId()).isEqualTo(franquiciaId);
        assertThat(guardada.getNombre()).isEqualTo("Burger Express");
        verify(franquiciaRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Deberia guardar una franquicia existente modificando su nombre")
    void guardaFranquiciaExistenteModificandoNombre()
    {
        FranquiciaEntity existente = new FranquiciaEntity(franquiciaId, "Burger Express");
        when(franquiciaRepository.findById(franquiciaId)).thenReturn(Optional.of(existente));
        when(franquiciaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Franquicia guardada = adapter.guardarFranquicia(new Franquicia(franquiciaId, "Pizza Rapida"));

        ArgumentCaptor<FranquiciaEntity> captor = ArgumentCaptor.forClass(FranquiciaEntity.class);
        verify(franquiciaRepository).save(captor.capture());
        assertThat(captor.getValue()).isSameAs(existente);
        assertThat(guardada.getId()).isEqualTo(franquiciaId);
        assertThat(guardada.getNombre()).isEqualTo("Pizza Rapida");
    }

    @Test
    @DisplayName("Deberia obtener una franquicia existente por su id")
    void obtieneFranquiciaExistentePorId()
    {
        when(franquiciaRepository.findById(franquiciaId))
                .thenReturn(Optional.of(new FranquiciaEntity(franquiciaId, "Burger Express")));

        Optional<Franquicia> franquicia = adapter.obtenerFranquiciaPorId(franquiciaId);

        assertThat(franquicia).isPresent();
        assertThat(franquicia.get().getNombre()).isEqualTo("Burger Express");
    }

    @Test
    @DisplayName("Deberia devolver vacio al obtener una franquicia que no existe")
    void noObtieneFranquiciaInexistente()
    {
        when(franquiciaRepository.findById(franquiciaId)).thenReturn(Optional.empty());

        assertThat(adapter.obtenerFranquiciaPorId(franquiciaId)).isEmpty();
    }

    @Test
    @DisplayName("Deberia obtener todas las franquicias cuando hay franquicias")
    void obtieneTodasLasFranquicias()
    {
        when(franquiciaRepository.findAll()).thenReturn(List.of(
                new FranquiciaEntity(UUID.randomUUID(), "Burger Express"),
                new FranquiciaEntity(UUID.randomUUID(), "Pizza Rapida")));

        List<Franquicia> franquicias = adapter.obtenerTodasLasFranquicias();

        assertThat(franquicias).extracting(Franquicia::getNombre)
                .containsExactlyInAnyOrder("Burger Express", "Pizza Rapida");
    }

    @Test
    @DisplayName("Deberia devolver una lista vacia cuando no hay franquicias")
    void obtieneListaVaciaSinFranquicias()
    {
        when(franquiciaRepository.findAll()).thenReturn(List.of());

        assertThat(adapter.obtenerTodasLasFranquicias()).isEmpty();
    }
}
