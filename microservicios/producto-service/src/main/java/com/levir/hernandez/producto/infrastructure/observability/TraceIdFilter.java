package com.levir.hernandez.producto.infrastructure.observability;

import io.micrometer.context.ContextRegistry;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

/**
 * Gestiona el traceo de las peticiones HTTP.
 *
 * Si la petición contiene un X-Trace-Id, reutiliza el identificador recibido
 * para mantener la trazabilidad entre servicios. En caso contrario, genera
 * un nuevo identificador para iniciar una nueva traza.
 *
 * En WebFlux una peticion salta entre hilos, por eso el identificador viaja en el contexto de Reactor
 * y la propagacion automatica lo copia al MDC en cada operador.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter implements WebFilter
{
    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String TRACE_ID_MDC_KEY = "traceId";

    static
    {
        // Enlaza la clave del contexto de Reactor con la del MDC
        ContextRegistry.getInstance().registerThreadLocalAccessor(TRACE_ID_MDC_KEY,
                () -> MDC.get(TRACE_ID_MDC_KEY),
                traceId -> MDC.put(TRACE_ID_MDC_KEY, traceId),
                () -> MDC.remove(TRACE_ID_MDC_KEY));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain)
    {
        // Obtiene el identificador de traza recibido o genera uno nuevo si no está presente
        String traceId = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(TRACE_ID_HEADER))
                .filter(header -> !header.isBlank())
                .orElseGet(() -> UUID.randomUUID().toString());

        // Lo propaga en la respuesta y en el contexto de toda la cadena reactiva de la peticion
        exchange.getResponse().getHeaders().set(TRACE_ID_HEADER, traceId);

        return chain.filter(exchange)
                .contextWrite(contexto -> contexto.put(TRACE_ID_MDC_KEY, traceId));
    }
}
