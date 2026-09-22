package com.levir.hernandez.franquicia.infrastructure.config;

import com.levir.hernandez.franquicia.application.port.in.franquicia.AgregarFranquiciaUseCase;
import com.levir.hernandez.franquicia.application.port.in.franquicia.ObtenerFranquiciasUseCase;
import com.levir.hernandez.franquicia.application.port.in.producto.AgregarProductoUseCase;
import com.levir.hernandez.franquicia.application.port.in.sucursal.AgregarSucursalUseCase;
import com.levir.hernandez.franquicia.domain.model.Franquicia;
import com.levir.hernandez.franquicia.domain.model.Sucursal;
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
 * Carga datos de ejemplo desde un JSON al arrancar
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer
{
    private final JsonMapper jsonMapper;
    private final ObtenerFranquiciasUseCase obtenerFranquicias;
    private final AgregarFranquiciaUseCase agregarFranquicia;
    private final AgregarSucursalUseCase agregarSucursal;
    private final AgregarProductoUseCase agregarProducto;

    @Value("${app.datos-iniciales.path:}")
    private String path;

    @EventListener(ApplicationReadyEvent.class)
    public void cargarDatosIniciales()
    {
        if (path.isBlank()) return;

        // Evita duplicar los datos en cada reinicio
        if (!obtenerFranquicias.obtenerFranquicias().isEmpty()) return;

        List<FranquiciaJson> franquicias = leerArchivo();
        franquicias.forEach(this::crearFranquicia);

        log.info("Datos iniciales cargados desde {}: {} franquicias", path, franquicias.size());
    }

    private void crearFranquicia(FranquiciaJson json)
    {
        Franquicia franquicia = agregarFranquicia.agregarFranquicia(json.nombre());

        for (SucursalJson sucursal : json.sucursales())
        {
            crearSucursal(franquicia.getId(), sucursal);
        }
    }

    private void crearSucursal(UUID franquiciaId, SucursalJson json)
    {
        Sucursal sucursal = agregarSucursal.agregarSucursal(franquiciaId, json.nombre());

        for (ProductoJson producto : json.productos())
        {
            agregarProducto.agregarProducto(sucursal.getId(), producto.nombre(), producto.stock());
        }
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

    // Estructura de interpretacion del JSON

    record FranquiciaJson(String nombre, List<SucursalJson> sucursales)
    {
        FranquiciaJson {sucursales = sucursales == null ? List.of() : sucursales;}
    }

    record SucursalJson(String nombre, List<ProductoJson> productos)
    {
        SucursalJson {productos = productos == null ? List.of() : productos;}
    }

    record ProductoJson(String nombre, Integer stock) {}
}
