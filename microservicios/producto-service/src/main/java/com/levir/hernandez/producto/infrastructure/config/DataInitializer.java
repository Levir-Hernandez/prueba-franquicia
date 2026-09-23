package com.levir.hernandez.producto.infrastructure.config;

import com.levir.hernandez.producto.application.port.out.ProductoRepositoryPort;
import com.levir.hernandez.producto.domain.model.Producto;
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
 * Usa el repositorio directamente y no AgregarProductoUseCase: los datos semilla ya referencian
 * sucursales con ids fijos, y validarlas obligaria a que sucursal-service arranque primero.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer
{
    private final JsonMapper jsonMapper;
    private final ProductoRepositoryPort productoRepository;

    @Value("${app.datos-iniciales.path:}")
    private String path;

    @EventListener(ApplicationReadyEvent.class)
    public void cargarDatosIniciales()
    {
        if (path.isBlank()) return;

        // Solo se insertan los ids que aun no existen: no duplica ni sobrescribe cambios hechos por la API
        List<ProductoJson> nuevos = leerArchivo().stream()
                .filter(json -> productoRepository.obtenerProductoPorId(json.id()).isEmpty())
                .toList();

        nuevos.forEach(json -> productoRepository.guardarProducto(
                new Producto(json.id(), json.nombre(), json.stock(), json.sucursalId())));

        log.info("Datos iniciales cargados desde {}: {} productos nuevos", path, nuevos.size());
    }

    private List<ProductoJson> leerArchivo()
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

    record ProductoJson(UUID id, String nombre, Integer stock, UUID sucursalId) {}
}
