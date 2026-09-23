package com.levir.hernandez.sucursal.infrastructure.config;

import com.levir.hernandez.sucursal.application.port.out.SucursalRepositoryPort;
import com.levir.hernandez.sucursal.domain.model.Sucursal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * Carga datos de ejemplo desde un JSON al arrancar.
 * Usa el repositorio directamente y no AgregarSucursalUseCase: los datos semilla ya referencian
 * franquicias con ids fijos, y validarlas obligaria a que franquicia-service arranque primero.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer
{
    private final JsonMapper jsonMapper;
    private final SucursalRepositoryPort sucursalRepository;

    @Value("${app.datos-iniciales.path:}")
    private String path;

    @EventListener(ApplicationReadyEvent.class)
    public void cargarDatosIniciales()
    {
        if (path.isBlank()) return;

        // Solo se insertan los ids que aun no existen: no duplica ni sobrescribe cambios hechos por la API
        List<SucursalJson> nuevos = leerArchivo().stream()
                .filter(json -> sucursalRepository.obtenerSucursalPorId(json.id()).isEmpty())
                .toList();

        nuevos.forEach(json -> sucursalRepository.guardarSucursal(
                new Sucursal(json.id(), json.nombre(), json.franquiciaId())));

        log.info("Datos iniciales cargados desde {}: {} sucursales nuevas", path, nuevos.size());
    }

    private List<SucursalJson> leerArchivo()
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

    record SucursalJson(UUID id, String nombre, UUID franquiciaId) {}
}
