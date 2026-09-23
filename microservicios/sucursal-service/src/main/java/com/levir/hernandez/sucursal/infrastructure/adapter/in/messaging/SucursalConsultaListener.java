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
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

/**
 * Atiende consultas JMS de otros servicios sobre sucursales
 * y propaga el trace ID del mensaje al contexto de logging.
 * El listener JMS es sincrono (request-reply): espera el resultado de los casos de uso reactivos
 * en su propio hilo, que no pertenece al event loop.
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
        return conTraza(traceId, Mono.fromCallable(() -> UUID.fromString(sucursalId))
                .flatMap(obtenerSucursal::obtenerSucursal)
                .map(sucursal -> "true")
                .onErrorReturn(error -> error instanceof SucursalNoEncontradaException
                        || error instanceof IllegalArgumentException, "false"));
    }

    @JmsListener(destination = "${app.mensajeria.colas.sucursales-por-franquicia}")
    public String porFranquicia(String franquiciaId,
                                @Header(name = TraceIdFilter.TRACE_ID_MDC_KEY, required = false) String traceId)
    {
        return conTraza(traceId, Mono.fromCallable(() -> UUID.fromString(franquiciaId))
                .flatMapMany(obtenerSucursales::obtenerSucursales)
                .map(sucursal -> new SucursalResumenMensaje(sucursal.getId(), sucursal.getNombre()))
                .collectList()
                .onErrorReturn(IllegalArgumentException.class, List.of())
                .map(jsonMapper::writeValueAsString));
    }

    private String conTraza(String traceId, Mono<String> respuesta)
    {
        if (traceId == null) return respuesta.block();

        MDC.put(TraceIdFilter.TRACE_ID_MDC_KEY, traceId);
        try {return respuesta.contextWrite(contexto -> contexto.put(TraceIdFilter.TRACE_ID_MDC_KEY, traceId)).block();}
        finally {MDC.remove(TraceIdFilter.TRACE_ID_MDC_KEY);}
    }

    record SucursalResumenMensaje(UUID id, String nombre) {}
}
