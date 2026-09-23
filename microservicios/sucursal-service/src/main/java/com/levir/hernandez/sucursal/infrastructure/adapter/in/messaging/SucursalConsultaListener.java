package com.levir.hernandez.sucursal.infrastructure.adapter.in.messaging;

import com.levir.hernandez.sucursal.application.exception.SucursalNoEncontradaException;
import com.levir.hernandez.sucursal.application.port.in.sucursal.ObtenerSucursalUseCase;
import com.levir.hernandez.sucursal.application.port.in.sucursal.ObtenerSucursalesUseCase;
import com.levir.hernandez.sucursal.infrastructure.observability.TraceIdFilter;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Atiende consultas JMS de otros servicios sobre sucursales
 * y propaga el trace ID del mensaje al contexto de logging.
 */
@Component
@RequiredArgsConstructor
public class SucursalConsultaListener
{
    private final ObtenerSucursalUseCase obtenerSucursal;
    private final ObtenerSucursalesUseCase obtenerSucursales;
    private final JsonMapper jsonMapper;

    @JmsListener(destination = "${app.mensajeria.colas.sucursal-existe}")
    public String existe(String sucursalId, @Header(name = TraceIdFilter.TRACE_ID_MDC_KEY, required = false) String traceId)
    {
        return conTraza(traceId, () ->
        {
            try
            {
                obtenerSucursal.obtenerSucursal(UUID.fromString(sucursalId));
                return "true";
            }
            catch (SucursalNoEncontradaException | IllegalArgumentException noExiste) {return "false";}
        });
    }

    @JmsListener(destination = "${app.mensajeria.colas.sucursales-por-franquicia}")
    public String porFranquicia(String franquiciaId,
                                @Header(name = TraceIdFilter.TRACE_ID_MDC_KEY, required = false) String traceId)
    {
        return conTraza(traceId, () ->
        {
            List<SucursalResumenMensaje> sucursales;
            try
            {
                sucursales = obtenerSucursales.obtenerSucursales(UUID.fromString(franquiciaId)).stream()
                        .map(sucursal -> new SucursalResumenMensaje(sucursal.getId(), sucursal.getNombre()))
                        .toList();
            }
            catch (IllegalArgumentException idInvalido) {sucursales = List.of();}

            return jsonMapper.writeValueAsString(sucursales);
        });
    }

    private String conTraza(String traceId, Supplier<String> respuesta)
    {
        if (traceId != null) MDC.put(TraceIdFilter.TRACE_ID_MDC_KEY, traceId);

        try {return respuesta.get();}
        finally {MDC.remove(TraceIdFilter.TRACE_ID_MDC_KEY);}
    }

    record SucursalResumenMensaje(UUID id, String nombre) {}
}
