package com.levir.hernandez.franquicia.infrastructure.adapter.in.messaging;

import com.levir.hernandez.franquicia.application.exception.FranquiciaNoEncontradaException;
import com.levir.hernandez.franquicia.application.port.in.franquicia.ObtenerFranquiciaUseCase;
import com.levir.hernandez.franquicia.infrastructure.observability.TraceIdFilter;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Atiende consultas JMS de otros servicios sobre franquicias
 * y propaga el trace ID del mensaje al contexto de logging.
 */
@Component
@RequiredArgsConstructor
public class FranquiciaConsultaListener
{
    private final ObtenerFranquiciaUseCase obtenerFranquicia;

    @JmsListener(destination = "${app.mensajeria.colas.franquicia-existe}")
    public String existe(String franquiciaId, @Header(name = TraceIdFilter.TRACE_ID_MDC_KEY, required = false) String traceId)
    {
        if (traceId != null) MDC.put(TraceIdFilter.TRACE_ID_MDC_KEY, traceId);

        try
        {
            obtenerFranquicia.obtenerFranquicia(UUID.fromString(franquiciaId));
            return "true";
        }
        catch (FranquiciaNoEncontradaException | IllegalArgumentException noExiste) {return "false";}
        finally {MDC.remove(TraceIdFilter.TRACE_ID_MDC_KEY);}
    }
}
