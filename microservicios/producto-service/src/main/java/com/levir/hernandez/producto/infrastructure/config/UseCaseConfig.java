package com.levir.hernandez.producto.infrastructure.config;

import com.levir.hernandez.producto.application.port.out.ProductoRepositoryPort;
import com.levir.hernandez.producto.application.port.out.SucursalConsultaPort;
import com.levir.hernandez.producto.application.service.AgregarProductoService;
import com.levir.hernandez.producto.application.service.ObtenerProductosConMayorStockService;
import com.levir.hernandez.producto.application.service.ProductoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion que registra las implementaciones de los casos de uso como beans de Spring,
 * manteniendo la capa de aplicacion independiente del framework.
 */
@Configuration
public class UseCaseConfig
{
    @Bean
    public ProductoService productoService(ProductoRepositoryPort productoRepository)
    {
        return new ProductoService(productoRepository);
    }

    @Bean
    public AgregarProductoService agregarProductoService(ProductoRepositoryPort productoRepository,
                                                         SucursalConsultaPort sucursalConsulta)
    {
        return new AgregarProductoService(productoRepository, sucursalConsulta);
    }

    @Bean
    public ObtenerProductosConMayorStockService obtenerProductosConMayorStockService(
            ProductoRepositoryPort productoRepository, SucursalConsultaPort sucursalConsulta)
    {
        return new ObtenerProductosConMayorStockService(productoRepository, sucursalConsulta);
    }
}
