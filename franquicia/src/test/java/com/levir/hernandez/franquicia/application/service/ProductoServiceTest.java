package com.levir.hernandez.franquicia.application.service;

import com.levir.hernandez.franquicia.application.exception.ProductoNoEncontradoException;
import com.levir.hernandez.franquicia.application.port.out.ProductoRepositoryPort;
import com.levir.hernandez.franquicia.domain.exception.ProductoInvalidoException;
import com.levir.hernandez.franquicia.domain.model.Producto;
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
class ProductoServiceTest
{
    @Mock
    private ProductoRepositoryPort productoRepository;

    @InjectMocks
    private ProductoService service;

    private final UUID sucursalId = UUID.randomUUID();
    private final UUID productoId = UUID.randomUUID();

    private Producto productoExistente()
    {
        return new Producto(productoId, "Hamburguesa clasica", 10, sucursalId);
    }

    @Test
    @DisplayName("Deberia agregar un producto nuevo asociado a su sucursal")
    void agregaProducto()
    {
        when(productoRepository.guardarProducto(any())).thenAnswer(inv -> inv.getArgument(0));

        Producto creado = service.agregarProducto(sucursalId, "Hamburguesa clasica", 10);

        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository).guardarProducto(captor.capture());
        assertThat(captor.getValue().getId()).isNull();
        assertThat(creado.getNombre()).isEqualTo("Hamburguesa clasica");
        assertThat(creado.getStock()).isEqualTo(10);
        assertThat(creado.getSucursalId()).isEqualTo(sucursalId);
    }

    @Test
    @DisplayName("Deberia lanzar excepcion y no guardar al agregar un producto con stock negativo")
    void noAgregaProductoConStockNegativo()
    {
        assertThatThrownBy(() -> service.agregarProducto(sucursalId, "Hamburguesa clasica", -1))
                .isInstanceOf(ProductoInvalidoException.class);
        verify(productoRepository, never()).guardarProducto(any());
    }

    @Test
    @DisplayName("Deberia obtener un producto existente por su id")
    void obtieneProductoExistente()
    {
        when(productoRepository.obtenerProductoPorId(productoId)).thenReturn(Optional.of(productoExistente()));

        assertThat(service.obtenerProducto(productoId).getNombre()).isEqualTo("Hamburguesa clasica");
    }

    @Test
    @DisplayName("Deberia lanzar excepcion al obtener un producto que no existe")
    void noObtieneProductoInexistente()
    {
        when(productoRepository.obtenerProductoPorId(productoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerProducto(productoId))
                .isInstanceOf(ProductoNoEncontradoException.class);
    }

    @Test
    @DisplayName("Deberia obtener los productos de una sucursal que tiene productos")
    void obtieneProductosDeSucursal()
    {
        when(productoRepository.obtenerProductosPorIdDeSucursal(sucursalId)).thenReturn(List.of(
                new Producto(UUID.randomUUID(), "Hamburguesa clasica", 10, sucursalId),
                new Producto(UUID.randomUUID(), "Papas fritas", 30, sucursalId)));

        assertThat(service.obtenerProductos(sucursalId)).extracting(Producto::getNombre)
                .containsExactlyInAnyOrder("Hamburguesa clasica", "Papas fritas");
    }

    @Test
    @DisplayName("Deberia devolver una lista vacia para una sucursal sin productos o que no existe")
    void obtieneListaVaciaSinProductos()
    {
        when(productoRepository.obtenerProductosPorIdDeSucursal(sucursalId)).thenReturn(List.of());

        assertThat(service.obtenerProductos(sucursalId)).isEmpty();
    }

    @Test
    @DisplayName("Deberia eliminar un producto que existe")
    void eliminaProductoExistente()
    {
        when(productoRepository.obtenerProductoPorId(productoId)).thenReturn(Optional.of(productoExistente()));

        service.eliminarProducto(productoId);

        verify(productoRepository).eliminarProductoPorId(productoId);
    }

    @Test
    @DisplayName("Deberia lanzar excepcion y no eliminar un producto que no existe")
    void noEliminaProductoInexistente()
    {
        when(productoRepository.obtenerProductoPorId(productoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminarProducto(productoId))
                .isInstanceOf(ProductoNoEncontradoException.class);
        verify(productoRepository, never()).eliminarProductoPorId(any());
    }

    @Test
    @DisplayName("Deberia modificar el stock de un producto existente")
    void modificaStockProducto()
    {
        when(productoRepository.obtenerProductoPorId(productoId)).thenReturn(Optional.of(productoExistente()));
        when(productoRepository.guardarProducto(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.modificarStockProducto(productoId, 25).getStock()).isEqualTo(25);
    }

    @Test
    @DisplayName("Deberia lanzar excepcion y no guardar al modificar el stock a un valor negativo")
    void noModificaStockNegativo()
    {
        when(productoRepository.obtenerProductoPorId(productoId)).thenReturn(Optional.of(productoExistente()));

        assertThatThrownBy(() -> service.modificarStockProducto(productoId, -1))
                .isInstanceOf(ProductoInvalidoException.class);
        verify(productoRepository, never()).guardarProducto(any());
    }

    @Test
    @DisplayName("Deberia lanzar excepcion y no guardar al modificar el stock de un producto que no existe")
    void noModificaStockProductoInexistente()
    {
        when(productoRepository.obtenerProductoPorId(productoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.modificarStockProducto(productoId, 25))
                .isInstanceOf(ProductoNoEncontradoException.class);
        verify(productoRepository, never()).guardarProducto(any());
    }

    @Test
    @DisplayName("Deberia renombrar un producto existente")
    void renombraProducto()
    {
        when(productoRepository.obtenerProductoPorId(productoId)).thenReturn(Optional.of(productoExistente()));
        when(productoRepository.guardarProducto(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.renombrarProducto(productoId, "Hamburguesa doble").getNombre())
                .isEqualTo("Hamburguesa doble");
    }

    @Test
    @DisplayName("Deberia lanzar excepcion y no guardar al renombrar un producto que no existe")
    void noRenombraProductoInexistente()
    {
        when(productoRepository.obtenerProductoPorId(productoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.renombrarProducto(productoId, "Hamburguesa doble"))
                .isInstanceOf(ProductoNoEncontradoException.class);
        verify(productoRepository, never()).guardarProducto(any());
    }
}
