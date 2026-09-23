package com.levir.hernandez.franquicia.infrastructure.config;

import com.levir.hernandez.franquicia.application.port.out.FranquiciaRepositoryPort;
import com.levir.hernandez.franquicia.domain.model.Franquicia;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * Carga datos de ejemplo desde un JSON al arrancar.
 * Los ids son fijos para que los otros servicios puedan referenciarlos en sus propios datos iniciales.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer
{
    private final JsonMapper jsonMapper;
    private final FranquiciaRepositoryPort franquiciaRepository;

    @Value("${app.datos-iniciales.path:}")
    private String path;

    @EventListener(ApplicationReadyEvent.class)
    public void cargarDatosIniciales()
    {
        if (path.isBlank()) return;

        // Solo se insertan los ids que aun no existen: no duplica ni sobrescribe cambios hechos por la API
        Flux.fromIterable(leerArchivo())
                .filterWhen(json -> franquiciaRepository.obtenerFranquiciaPorId(json.id()).hasElement().map(existe -> !existe))
                .concatMap(json -> franquiciaRepository.guardarFranquicia(new Franquicia(json.id(), json.nombre())))
                .count()
                .subscribe(
                        nuevos -> log.info("Datos iniciales cargados desde {}: {} franquicias nuevas", path, nuevos),
                        error -> log.warn("No se pudieron cargar los datos iniciales de {}: {}", path, error.toString()));
    }

    private List<FranquiciaJson> leerArchivo()
    {
        try (InputStream contenido = new ClassPathResource(path).getInputStream())
        {
            return jsonMapper.readValue(contenido, new TypeReference<>() {});
        }
        catch (IOException e)
        {
            log.warn("No se pudieron leer los datos iniciales de {}: {}", path, e.getMessage());
            return List.of();
        }
    }

    record FranquiciaJson(UUID id, String nombre) {}
}
