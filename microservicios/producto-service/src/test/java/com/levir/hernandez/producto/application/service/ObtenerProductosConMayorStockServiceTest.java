package com.levir.hernandez.producto.application.service;

import com.levir.hernandez.producto.application.port.out.ProductoConMayorStock;
import com.levir.hernandez.producto.application.port.out.ProductoRepositoryPort;
import com.levir.hernandez.producto.application.port.out.SucursalConsultaPort;
import com.levir.hernandez.producto.application.port.out.SucursalResumen;
import com.levir.hernandez.producto.domain.model.Producto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObtenerProductosConMayorStockServiceTest
{
    @Mock
    private ProductoRepositoryPort productoRepository;

    @Mock
    private SucursalConsultaPort sucursalConsulta;

    @InjectMocks
    private ObtenerProductosConMayorStockService service;

    private final UUID franquiciaId = UUID.randomUUID();
    private final UUID sucursalId = UUID.randomUUID();

    @Test
    @DisplayName("Deberia devolver los productos con mayor stock incluyendo empates y el nombre de su sucursal")
    void obtieneProductosConMayorStockConEmpates()
    {
        Producto hamburguesa = new Producto(UUID.randomUUID(), "Hamburguesa clasica", 30, sucursalId);
        Producto papas = new Producto(UUID.randomUUID(), "Papas fritas", 30, sucursalId);
        when(sucursalConsulta.obtenerSucursalesDeFranquicia(franquiciaId))
                .thenReturn(List.of(new SucursalResumen(sucursalId, "Burger Express Centro")));
        when(productoRepository.obtenerProductosConMayorStockPorIdsDeSucursal(Set.of(sucursalId)))
                .thenReturn(List.of(hamburguesa, papas));

        assertThat(service.obtenerProductosConMayorStock(franquiciaId)).containsExactly(
                new ProductoConMayorStock(hamburguesa.getId(), "Hamburguesa clasica", 30, sucursalId, "Burger Express Centro"),
                new ProductoConMayorStock(papas.getId(), "Papas fritas", 30, sucursalId, "Burger Express Centro"));
    }

    @Test
    @DisplayName("Deberia devolver una lista vacia sin consultar productos si la franquicia no tiene sucursales")
    void obtieneListaVaciaSinSucursales()
    {
        // Tambien cubre el fallback del circuit breaker, que devuelve una lista vacia
        when(sucursalConsulta.obtenerSucursalesDeFranquicia(franquiciaId)).thenReturn(List.of());

        assertThat(service.obtenerProductosConMayorStock(franquiciaId)).isEmpty();
        verifyNoInteractions(productoRepository);
    }

    @Test
    @DisplayName("Deberia devolver una lista vacia si las sucursales de la franquicia no tienen productos")
    void obtieneListaVaciaSinProductos()
    {
        when(sucursalConsulta.obtenerSucursalesDeFranquicia(franquiciaId))
                .thenReturn(List.of(new SucursalResumen(sucursalId, "Burger Express Centro")));
        when(productoRepository.obtenerProductosConMayorStockPorIdsDeSucursal(Set.of(sucursalId))).thenReturn(List.of());

        assertThat(service.obtenerProductosConMayorStock(franquiciaId)).isEmpty();
    }
}
