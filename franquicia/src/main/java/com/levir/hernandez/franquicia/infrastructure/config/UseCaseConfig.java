package com.levir.hernandez.franquicia.infrastructure.config;

import com.levir.hernandez.franquicia.application.port.out.FranquiciaRepositoryPort;
import com.levir.hernandez.franquicia.application.port.out.ProductoRepositoryPort;
import com.levir.hernandez.franquicia.application.port.out.SucursalRepositoryPort;
import com.levir.hernandez.franquicia.application.service.FranquiciaService;
import com.levir.hernandez.franquicia.application.service.ObtenerProductosConMayorStockService;
import com.levir.hernandez.franquicia.application.service.ProductoService;
import com.levir.hernandez.franquicia.application.service.SucursalService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion que registra las implementaciones de los casos de uso como beans de Spring,
 * manteniendo la capa de aplicación independiente del framework.
 */
@Configuration
public class UseCaseConfig
{
    @Bean
    public FranquiciaService franquiciaService(FranquiciaRepositoryPort franquiciaRepository)
    {
        return new FranquiciaService(franquiciaRepository);
    }

    @Bean
    public SucursalService sucursalService(SucursalRepositoryPort sucursalRepository)
    {
        return new SucursalService(sucursalRepository);
    }

    @Bean
    public ProductoService productoService(ProductoRepositoryPort productoRepository)
    {
        return new ProductoService(productoRepository);
    }

    @Bean
    public ObtenerProductosConMayorStockService obtenerProductosConMayorStockService(
            ProductoRepositoryPort productoRepository)
    {
        return new ObtenerProductosConMayorStockService(productoRepository);
    }
}
