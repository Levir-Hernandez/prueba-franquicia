package com.levir.hernandez.sucursal.infrastructure.config;

import com.levir.hernandez.sucursal.application.port.out.FranquiciaConsultaPort;
import com.levir.hernandez.sucursal.application.port.out.SucursalRepositoryPort;
import com.levir.hernandez.sucursal.application.service.AgregarSucursalService;
import com.levir.hernandez.sucursal.application.service.SucursalService;
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
    public SucursalService sucursalService(SucursalRepositoryPort sucursalRepository)
    {
        return new SucursalService(sucursalRepository);
    }

    @Bean
    public AgregarSucursalService agregarSucursalService(SucursalRepositoryPort sucursalRepository,
                                                         FranquiciaConsultaPort franquiciaConsulta)
    {
        return new AgregarSucursalService(sucursalRepository, franquiciaConsulta);
    }
}
