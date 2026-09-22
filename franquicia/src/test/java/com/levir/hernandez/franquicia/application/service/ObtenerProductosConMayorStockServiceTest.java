package com.levir.hernandez.franquicia.application.service;

import com.levir.hernandez.franquicia.application.port.out.ProductoConMayorStock;
import com.levir.hernandez.franquicia.application.port.out.ProductoRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObtenerProductosConMayorStockServiceTest
{
    @Mock
    private ProductoRepositoryPort productoRepository;

    @InjectMocks
    private ObtenerProductosConMayorStockService service;

    private final UUID franquiciaId = UUID.randomUUID();
    private final UUID sucursalId = UUID.randomUUID();

    @Test
    @DisplayName("Deberia devolver los productos con mayor stock incluyendo empates")
    void obtieneProductosConMayorStockConEmpates()
    {
        List<ProductoConMayorStock> esperados = List.of(
                new ProductoConMayorStock(UUID.randomUUID(), "Hamburguesa clasica", 30, sucursalId, "Burger Express Centro"),
                new ProductoConMayorStock(UUID.randomUUID(), "Papas fritas", 30, sucursalId, "Burger Express Centro"));
        when(productoRepository.obtenerProductosConMayorStockPorIdDeFranquicia(franquiciaId)).thenReturn(esperados);

        assertThat(service.obtenerProductosConMayorStock(franquiciaId)).containsExactlyElementsOf(esperados);
    }

    @Test
    @DisplayName("Deberia devolver una lista vacia para una franquicia sin productos o que no existe")
    void obtieneListaVaciaSinProductos()
    {
        when(productoRepository.obtenerProductosConMayorStockPorIdDeFranquicia(franquiciaId)).thenReturn(List.of());

        assertThat(service.obtenerProductosConMayorStock(franquiciaId)).isEmpty();
    }
}
