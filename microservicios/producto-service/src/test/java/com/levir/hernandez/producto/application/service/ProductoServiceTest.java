package com.levir.hernandez.producto.application.service;

import com.levir.hernandez.producto.application.exception.ProductoNoEncontradoException;
import com.levir.hernandez.producto.application.port.out.ProductoRepositoryPort;
import com.levir.hernandez.producto.domain.exception.ProductoInvalidoException;
import com.levir.hernandez.producto.domain.model.Producto;
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
    @DisplayName("Deberia obtener un producto existente por su id")
    void obtieneProductoExistente()
    {
        when(productoRepository.obtenerProductoPorId(productoId)).thenReturn(Mono.just(productoExistente()));

        StepVerifier.create(service.obtenerProducto(productoId).map(Producto::getNombre))
                .expectNext("Hamburguesa clasica")
                .verifyComplete();
    }

    @Test
    @DisplayName("Deberia emitir error al obtener un producto que no existe")
    void noObtieneProductoInexistente()
    {
        when(productoRepository.obtenerProductoPorId(productoId)).thenReturn(Mono.empty());

        StepVerifier.create(service.obtenerProducto(productoId))
                .expectError(ProductoNoEncontradoException.class)
                .verify();
    }

    @Test
    @DisplayName("Deberia obtener los productos de una sucursal que tiene productos")
    void obtieneProductosDeSucursal()
    {
        when(productoRepository.obtenerProductosPorIdDeSucursal(sucursalId)).thenReturn(Flux.just(
                new Producto(UUID.randomUUID(), "Hamburguesa clasica", 10, sucursalId),
                new Producto(UUID.randomUUID(), "Papas fritas", 30, sucursalId)));

        StepVerifier.create(service.obtenerProductos(sucursalId).map(Producto::getNombre))
                .expectNext("Hamburguesa clasica", "Papas fritas")
                .verifyComplete();
    }

    @Test
    @DisplayName("Deberia terminar vacio para una sucursal sin productos o que no existe")
    void obtieneListaVaciaSinProductos()
    {
        when(productoRepository.obtenerProductosPorIdDeSucursal(sucursalId)).thenReturn(Flux.empty());

        StepVerifier.create(service.obtenerProductos(sucursalId)).verifyComplete();
    }

    @Test
    @DisplayName("Deberia eliminar un producto que existe")
    void eliminaProductoExistente()
    {
        when(productoRepository.obtenerProductoPorId(productoId)).thenReturn(Mono.just(productoExistente()));
        when(productoRepository.eliminarProductoPorId(productoId)).thenReturn(Mono.empty());

        StepVerifier.create(service.eliminarProducto(productoId)).verifyComplete();

        verify(productoRepository).eliminarProductoPorId(productoId);
    }

    @Test
    @DisplayName("Deberia emitir error y no eliminar un producto que no existe")
    void noEliminaProductoInexistente()
    {
        when(productoRepository.obtenerProductoPorId(productoId)).thenReturn(Mono.empty());

        StepVerifier.create(service.eliminarProducto(productoId))
                .expectError(ProductoNoEncontradoException.class)
                .verify();

        verify(productoRepository, never()).eliminarProductoPorId(any());
    }

    @Test
    @DisplayName("Deberia modificar el stock de un producto existente")
    void modificaStockProducto()
    {
        when(productoRepository.obtenerProductoPorId(productoId)).thenReturn(Mono.just(productoExistente()));
        when(productoRepository.guardarProducto(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.modificarStockProducto(productoId, 25).map(Producto::getStock))
                .expectNext(25)
                .verifyComplete();
    }

    @Test
    @DisplayName("Deberia emitir error y no guardar al modificar el stock a un valor negativo")
    void noModificaStockNegativo()
    {
        when(productoRepository.obtenerProductoPorId(productoId)).thenReturn(Mono.just(productoExistente()));

        StepVerifier.create(service.modificarStockProducto(productoId, -1))
                .expectError(ProductoInvalidoException.class)
                .verify();

        verify(productoRepository, never()).guardarProducto(any());
    }

    @Test
    @DisplayName("Deberia emitir error y no guardar al modificar el stock de un producto que no existe")
    void noModificaStockProductoInexistente()
    {
        when(productoRepository.obtenerProductoPorId(productoId)).thenReturn(Mono.empty());

        StepVerifier.create(service.modificarStockProducto(productoId, 25))
                .expectError(ProductoNoEncontradoException.class)
                .verify();

        verify(productoRepository, never()).guardarProducto(any());
    }

    @Test
    @DisplayName("Deberia renombrar un producto existente")
    void renombraProducto()
    {
        when(productoRepository.obtenerProductoPorId(productoId)).thenReturn(Mono.just(productoExistente()));
        when(productoRepository.guardarProducto(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.renombrarProducto(productoId, "Hamburguesa doble").map(Producto::getNombre))
                .expectNext("Hamburguesa doble")
                .verifyComplete();
    }

    @Test
    @DisplayName("Deberia emitir error y no guardar al renombrar un producto que no existe")
    void noRenombraProductoInexistente()
    {
        when(productoRepository.obtenerProductoPorId(productoId)).thenReturn(Mono.empty());

        StepVerifier.create(service.renombrarProducto(productoId, "Hamburguesa doble"))
                .expectError(ProductoNoEncontradoException.class)
                .verify();

        verify(productoRepository, never()).guardarProducto(any());
    }
}
