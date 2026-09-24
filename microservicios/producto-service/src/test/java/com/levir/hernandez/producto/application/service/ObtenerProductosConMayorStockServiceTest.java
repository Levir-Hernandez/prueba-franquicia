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
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Set;
import java.util.UUID;

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
                .thenReturn(Flux.just(new SucursalResumen(sucursalId, "Burger Express Centro")));
        when(productoRepository.obtenerProductosConMayorStockPorIdsDeSucursal(Set.of(sucursalId)))
                .thenReturn(Flux.just(hamburguesa, papas));

        StepVerifier.create(service.obtenerProductosConMayorStock(franquiciaId))
                .expectNext(new ProductoConMayorStock(hamburguesa.getId(), "Hamburguesa clasica", 30, sucursalId,
                        "Burger Express Centro"))
                .expectNext(new ProductoConMayorStock(papas.getId(), "Papas fritas", 30, sucursalId,
                        "Burger Express Centro"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Deberia terminar vacio sin consultar productos si la franquicia no tiene sucursales")
    void obtieneListaVaciaSinSucursales()
    {
        // Tambien cubre el fallback del circuit breaker, que termina vacio
        when(sucursalConsulta.obtenerSucursalesDeFranquicia(franquiciaId)).thenReturn(Flux.empty());

        StepVerifier.create(service.obtenerProductosConMayorStock(franquiciaId)).verifyComplete();
        verifyNoInteractions(productoRepository);
    }

    @Test
    @DisplayName("Deberia terminar vacio si las sucursales de la franquicia no tienen productos")
    void obtieneListaVaciaSinProductos()
    {
        when(sucursalConsulta.obtenerSucursalesDeFranquicia(franquiciaId))
                .thenReturn(Flux.just(new SucursalResumen(sucursalId, "Burger Express Centro")));
        when(productoRepository.obtenerProductosConMayorStockPorIdsDeSucursal(Set.of(sucursalId)))
                .thenReturn(Flux.empty());

        StepVerifier.create(service.obtenerProductosConMayorStock(franquiciaId)).verifyComplete();
    }
}
