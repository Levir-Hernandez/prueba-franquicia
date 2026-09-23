package com.levir.hernandez.franquicia.infrastructure.config;

import com.levir.hernandez.franquicia.application.port.out.FranquiciaRepositoryPort;
import com.levir.hernandez.franquicia.application.service.FranquiciaService;
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
    public FranquiciaService franquiciaService(FranquiciaRepositoryPort franquiciaRepository)
    {
        return new FranquiciaService(franquiciaRepository);
    }
}
