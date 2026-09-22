package com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence;

import com.levir.hernandez.franquicia.domain.model.Sucursal;
import com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.entity.FranquiciaEntity;
import com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.entity.SucursalEntity;
import com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.repository.SucursalJpaRepository;
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
class SucursalRepositoryAdapterTest
{
    @Mock
    private SucursalJpaRepository sucursalRepository;

    @InjectMocks
    private SucursalRepositoryAdapter adapter;

    private final UUID franquiciaId = UUID.randomUUID();
    private final UUID sucursalId = UUID.randomUUID();

    private SucursalEntity sucursalEntity(UUID id, String nombre)
    {
        return new SucursalEntity(id, nombre, new FranquiciaEntity(franquiciaId));
    }

    @Test
    @DisplayName("Deberia guardar una sucursal nueva asociada a su franquicia")
    void guardaSucursalNueva()
    {
        when(sucursalRepository.save(any())).thenAnswer(inv -> {
            SucursalEntity entity = inv.getArgument(0);
            entity.setId(sucursalId);
            return entity;
        });

        Sucursal guardada = adapter.guardarSucursal(new Sucursal(null, "Burger Express Centro", franquiciaId));

        assertThat(guardada.getId()).isEqualTo(sucursalId);
        assertThat(guardada.getNombre()).isEqualTo("Burger Express Centro");
        assertThat(guardada.getFranquiciaId()).isEqualTo(franquiciaId);
        verify(sucursalRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Deberia guardar una sucursal existente modificando su nombre")
    void guardaSucursalExistenteModificandoNombre()
    {
        SucursalEntity existente = sucursalEntity(sucursalId, "Burger Express Centro");
        when(sucursalRepository.findById(sucursalId)).thenReturn(Optional.of(existente));
        when(sucursalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Sucursal guardada = adapter.guardarSucursal(new Sucursal(sucursalId, "Burger Express Poblado", franquiciaId));

        ArgumentCaptor<SucursalEntity> captor = ArgumentCaptor.forClass(SucursalEntity.class);
        verify(sucursalRepository).save(captor.capture());
        assertThat(captor.getValue()).isSameAs(existente);
        assertThat(guardada.getId()).isEqualTo(sucursalId);
        assertThat(guardada.getNombre()).isEqualTo("Burger Express Poblado");
    }

    @Test
    @DisplayName("Deberia obtener una sucursal existente por su id")
    void obtieneSucursalExistentePorId()
    {
        when(sucursalRepository.findById(sucursalId))
                .thenReturn(Optional.of(sucursalEntity(sucursalId, "Burger Express Centro")));

        Optional<Sucursal> sucursal = adapter.obtenerSucursalPorId(sucursalId);

        assertThat(sucursal).isPresent();
        assertThat(sucursal.get().getNombre()).isEqualTo("Burger Express Centro");
        assertThat(sucursal.get().getFranquiciaId()).isEqualTo(franquiciaId);
    }

    @Test
    @DisplayName("Deberia devolver vacio al obtener una sucursal que no existe")
    void noObtieneSucursalInexistente()
    {
        when(sucursalRepository.findById(sucursalId)).thenReturn(Optional.empty());

        assertThat(adapter.obtenerSucursalPorId(sucursalId)).isEmpty();
    }

    @Test
    @DisplayName("Deberia obtener las sucursales de una franquicia que tiene sucursales")
    void obtieneSucursalesDeFranquicia()
    {
        when(sucursalRepository.findByFranquiciaId(franquiciaId)).thenReturn(List.of(
                sucursalEntity(UUID.randomUUID(), "Burger Express Centro"),
                sucursalEntity(UUID.randomUUID(), "Burger Express Poblado")));

        List<Sucursal> sucursales = adapter.obtenerSucursalesPorIdDeFranquicia(franquiciaId);

        assertThat(sucursales).extracting(Sucursal::getNombre)
                .containsExactlyInAnyOrder("Burger Express Centro", "Burger Express Poblado");
        assertThat(sucursales).allMatch(sucursal -> sucursal.getFranquiciaId().equals(franquiciaId));
    }

    @Test
    @DisplayName("Deberia devolver una lista vacia para una franquicia sin sucursales o que no existe")
    void obtieneListaVaciaSinSucursales()
    {
        when(sucursalRepository.findByFranquiciaId(franquiciaId)).thenReturn(List.of());

        assertThat(adapter.obtenerSucursalesPorIdDeFranquicia(franquiciaId)).isEmpty();
    }
}
