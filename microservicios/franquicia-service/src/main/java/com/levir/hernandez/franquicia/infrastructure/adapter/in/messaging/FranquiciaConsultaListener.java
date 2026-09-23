package com.levir.hernandez.franquicia.infrastructure.adapter.in.messaging;

import com.levir.hernandez.franquicia.application.exception.FranquiciaNoEncontradaException;
import com.levir.hernandez.franquicia.application.port.in.franquicia.ObtenerFranquiciaUseCase;
import com.levir.hernandez.franquicia.infrastructure.observability.TraceIdFilter;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Atiende consultas JMS de otros servicios sobre franquicias
 * y propaga el trace ID del mensaje al contexto de logging.
 * El listener JMS es sincrono (request-reply): espera el resultado del caso de uso reactivo
 * en su propio hilo, que no pertenece al event loop.
 */
@Component
@RequiredArgsConstructor
public class FranquiciaConsultaListener
{
    private final ObtenerFranquiciaUseCase obtenerFranquicia;

    @JmsListener(destination = "${app.mensajeria.colas.franquicia-existe}")
    public String existe(String franquiciaId, @Header(name = TraceIdFilter.TRACE_ID_MDC_KEY, required = false) String traceId)
    {
        Mono<String> respuesta = Mono.fromCallable(() -> UUID.fromString(franquiciaId))
                .flatMap(obtenerFranquicia::obtenerFranquicia)
                .map(franquicia -> "true")
                .onErrorReturn(error -> error instanceof FranquiciaNoEncontradaException
                        || error instanceof IllegalArgumentException, "false");

        if (traceId == null) return respuesta.block();

        MDC.put(TraceIdFilter.TRACE_ID_MDC_KEY, traceId);
        try {return respuesta.contextWrite(contexto -> contexto.put(TraceIdFilter.TRACE_ID_MDC_KEY, traceId)).block();}
        finally {MDC.remove(TraceIdFilter.TRACE_ID_MDC_KEY);}
    }
}
