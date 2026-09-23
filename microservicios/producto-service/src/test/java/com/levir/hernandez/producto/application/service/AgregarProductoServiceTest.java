package com.levir.hernandez.producto.application.service;

import com.levir.hernandez.producto.application.exception.ServicioNoDisponibleException;
import com.levir.hernandez.producto.application.exception.SucursalNoEncontradaException;
import com.levir.hernandez.producto.application.port.out.ProductoRepositoryPort;
import com.levir.hernandez.producto.application.port.out.SucursalConsultaPort;
import com.levir.hernandez.producto.domain.exception.ProductoInvalidoException;
import com.levir.hernandez.producto.domain.model.Producto;
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
class AgregarProductoServiceTest
{
    @Mock
    private ProductoRepositoryPort productoRepository;

    @Mock
    private SucursalConsultaPort sucursalConsulta;

    @InjectMocks
    private AgregarProductoService service;

    private final UUID sucursalId = UUID.randomUUID();

    @Test
    @DisplayName("Deberia agregar un producto nuevo asociado a su sucursal")
    void agregaProducto()
    {
        when(sucursalConsulta.existeSucursal(sucursalId)).thenReturn(Mono.just(true));
        when(productoRepository.guardarProducto(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.agregarProducto(sucursalId, "Hamburguesa clasica", 10))
                .assertNext(creado ->
                {
                    assertThat(creado.getNombre()).isEqualTo("Hamburguesa clasica");
                    assertThat(creado.getStock()).isEqualTo(10);
                    assertThat(creado.getSucursalId()).isEqualTo(sucursalId);
                })
                .verifyComplete();

        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository).guardarProducto(captor.capture());
        assertThat(captor.getValue().getId()).isNull();
    }

    @Test
    @DisplayName("Deberia emitir error y no guardar al agregar un producto con stock negativo")
    void noAgregaProductoConStockNegativo()
    {
        StepVerifier.create(service.agregarProducto(sucursalId, "Hamburguesa clasica", -1))
                .expectError(ProductoInvalidoException.class)
                .verify();

        verifyNoInteractions(sucursalConsulta);
        verify(productoRepository, never()).guardarProducto(any());
    }

    @Test
    @DisplayName("Deberia emitir error y no guardar al agregar un producto a una sucursal que no existe")
    void noAgregaProductoASucursalInexistente()
    {
        when(sucursalConsulta.existeSucursal(sucursalId)).thenReturn(Mono.just(false));

        StepVerifier.create(service.agregarProducto(sucursalId, "Hamburguesa clasica", 10))
                .expectError(SucursalNoEncontradaException.class)
                .verify();

        verify(productoRepository, never()).guardarProducto(any());
    }

    @Test
    @DisplayName("Deberia propagar el error y no guardar si el servicio de sucursales no esta disponible")
    void noAgregaProductoSiServicioNoDisponible()
    {
        when(sucursalConsulta.existeSucursal(sucursalId))
                .thenReturn(Mono.error(new ServicioNoDisponibleException("sucursales")));

        StepVerifier.create(service.agregarProducto(sucursalId, "Hamburguesa clasica", 10))
                .expectError(ServicioNoDisponibleException.class)
                .verify();

        verify(productoRepository, never()).guardarProducto(any());
    }
}
