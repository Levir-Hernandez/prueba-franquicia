package com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence;

import com.levir.hernandez.franquicia.application.port.out.ProductoConMayorStock;
import com.levir.hernandez.franquicia.domain.model.Producto;
import com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.entity.ProductoEntity;
import com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.entity.SucursalEntity;
import com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.repository.ProductoJpaRepository;
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
class ProductoRepositoryAdapterTest
{
    @Mock
    private ProductoJpaRepository productoRepository;

    @InjectMocks
    private ProductoRepositoryAdapter adapter;

    private final UUID franquiciaId = UUID.randomUUID();
    private final UUID sucursalId = UUID.randomUUID();
    private final UUID productoId = UUID.randomUUID();

    private ProductoEntity productoEntity(UUID id, String nombre, Integer stock)
    {
        return new ProductoEntity(id, nombre, stock, new SucursalEntity(sucursalId));
    }

    @Test
    @DisplayName("Deberia guardar un producto nuevo asociado a su sucursal")
    void guardaProductoNuevo()
    {
        when(productoRepository.save(any())).thenAnswer(inv -> {
            ProductoEntity entity = inv.getArgument(0);
            entity.setId(productoId);
            return entity;
        });

        Producto guardado = adapter.guardarProducto(new Producto(null, "Hamburguesa clasica", 10, sucursalId));

        assertThat(guardado.getId()).isEqualTo(productoId);
        assertThat(guardado.getNombre()).isEqualTo("Hamburguesa clasica");
        assertThat(guardado.getStock()).isEqualTo(10);
        assertThat(guardado.getSucursalId()).isEqualTo(sucursalId);
        verify(productoRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Deberia guardar un producto existente modificando su nombre y stock")
    void guardaProductoExistenteModificandoNombreYStock()
    {
        ProductoEntity existente = productoEntity(productoId, "Hamburguesa clasica", 10);
        when(productoRepository.findById(productoId)).thenReturn(Optional.of(existente));
        when(productoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Producto guardado = adapter.guardarProducto(new Producto(productoId, "Hamburguesa doble", 25, sucursalId));

        ArgumentCaptor<ProductoEntity> captor = ArgumentCaptor.forClass(ProductoEntity.class);
        verify(productoRepository).save(captor.capture());
        assertThat(captor.getValue()).isSameAs(existente);
        assertThat(guardado.getNombre()).isEqualTo("Hamburguesa doble");
        assertThat(guardado.getStock()).isEqualTo(25);
    }

    @Test
    @DisplayName("Deberia obtener un producto existente por su id")
    void obtieneProductoExistentePorId()
    {
        when(productoRepository.findById(productoId))
                .thenReturn(Optional.of(productoEntity(productoId, "Hamburguesa clasica", 10)));

        Optional<Producto> producto = adapter.obtenerProductoPorId(productoId);

        assertThat(producto).isPresent();
        assertThat(producto.get().getNombre()).isEqualTo("Hamburguesa clasica");
        assertThat(producto.get().getSucursalId()).isEqualTo(sucursalId);
    }

    @Test
    @DisplayName("Deberia devolver vacio al obtener un producto que no existe")
    void noObtieneProductoInexistente()
    {
        when(productoRepository.findById(productoId)).thenReturn(Optional.empty());

        assertThat(adapter.obtenerProductoPorId(productoId)).isEmpty();
    }

    @Test
    @DisplayName("Deberia obtener los productos de una sucursal que tiene productos")
    void obtieneProductosDeSucursal()
    {
        when(productoRepository.findBySucursalId(sucursalId)).thenReturn(List.of(
                productoEntity(UUID.randomUUID(), "Hamburguesa clasica", 10),
                productoEntity(UUID.randomUUID(), "Papas fritas", 30)));

        List<Producto> productos = adapter.obtenerProductosPorIdDeSucursal(sucursalId);

        assertThat(productos).extracting(Producto::getNombre)
                .containsExactlyInAnyOrder("Hamburguesa clasica", "Papas fritas");
    }

    @Test
    @DisplayName("Deberia devolver una lista vacia para una sucursal sin productos o que no existe")
    void obtieneListaVaciaSinProductos()
    {
        when(productoRepository.findBySucursalId(sucursalId)).thenReturn(List.of());

        assertThat(adapter.obtenerProductosPorIdDeSucursal(sucursalId)).isEmpty();
    }

    @Test
    @DisplayName("Deberia eliminar un producto que existe")
    void eliminaProductoExistente()
    {
        adapter.eliminarProductoPorId(productoId);

        verify(productoRepository).deleteById(productoId);
    }

    @Test
    @DisplayName("Deberia eliminar sin error un producto que no existe")
    void eliminaProductoInexistenteSinError()
    {
        UUID inexistente = UUID.randomUUID();

        adapter.eliminarProductoPorId(inexistente);

        verify(productoRepository).deleteById(inexistente);
    }

    @Test
    @DisplayName("Deberia devolver los productos con mayor stock incluyendo empates")
    void obtieneProductosConMayorStockConEmpates()
    {
        List<ProductoConMayorStock> esperados = List.of(
                new ProductoConMayorStock(UUID.randomUUID(), "Hamburguesa clasica", 30, sucursalId, "Burger Express Centro"),
                new ProductoConMayorStock(UUID.randomUUID(), "Papas fritas", 30, sucursalId, "Burger Express Centro"));
        when(productoRepository.obtenerProductosConMayorStockPorIdDeFranquicia(franquiciaId)).thenReturn(esperados);

        assertThat(adapter.obtenerProductosConMayorStockPorIdDeFranquicia(franquiciaId))
                .containsExactlyElementsOf(esperados);
    }

    @Test
    @DisplayName("Deberia devolver una lista vacia de mayor stock para una franquicia que no existe")
    void obtieneListaVaciaDeMayorStockSinFranquicia()
    {
        when(productoRepository.obtenerProductosConMayorStockPorIdDeFranquicia(franquiciaId)).thenReturn(List.of());

        assertThat(adapter.obtenerProductosConMayorStockPorIdDeFranquicia(franquiciaId)).isEmpty();
    }
}
