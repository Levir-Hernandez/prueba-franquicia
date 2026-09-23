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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private final UUID productoId = UUID.randomUUID();

    private Producto productoExistente()
    {
        return new Producto(productoId, "Hamburguesa clasica", 10, sucursalId);
    }

    @Test
    @DisplayName("Deberia agregar un producto nuevo asociado a su sucursal")
    void agregaProducto()
    {
        when(sucursalConsulta.existeSucursal(sucursalId)).thenReturn(true);
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
        verifyNoInteractions(sucursalConsulta);
        verify(productoRepository, never()).guardarProducto(any());
    }

    @Test
    @DisplayName("Deberia lanzar excepcion y no guardar al agregar un producto a una sucursal que no existe")
    void noAgregaProductoASucursalInexistente()
    {
        when(sucursalConsulta.existeSucursal(sucursalId)).thenReturn(false);

        assertThatThrownBy(() -> service.agregarProducto(sucursalId, "Hamburguesa clasica", 10))
                .isInstanceOf(SucursalNoEncontradaException.class);
        verify(productoRepository, never()).guardarProducto(any());
    }

    @Test
    @DisplayName("Deberia propagar la excepcion y no guardar si el servicio de sucursales no esta disponible")
    void noAgregaProductoSiServicioNoDisponible()
    {
        when(sucursalConsulta.existeSucursal(sucursalId)).thenThrow(new ServicioNoDisponibleException("sucursales"));

        assertThatThrownBy(() -> service.agregarProducto(sucursalId, "Hamburguesa clasica", 10))
                .isInstanceOf(ServicioNoDisponibleException.class);
        verify(productoRepository, never()).guardarProducto(any());
    }
}
