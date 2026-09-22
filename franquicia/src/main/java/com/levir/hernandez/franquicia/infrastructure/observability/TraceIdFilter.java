package com.levir.hernandez.franquicia.infrastructure.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * Gestiona el traceo de las peticiones HTTP.
 *
 * Si la petición contiene un X-Trace-Id, reutiliza el identificador recibido
 * para mantener la trazabilidad entre servicios. En caso contrario, genera
 * un nuevo identificador para iniciar una nueva traza.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter
{
    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String TRACE_ID_MDC_KEY = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException
    {
        // Obtiene el identificador de traza recibido o genera uno nuevo si no está presente
        String traceId = Optional.ofNullable(request.getHeader(TRACE_ID_HEADER))
                .filter(header -> !header.isBlank())
                .orElseGet(() -> UUID.randomUUID().toString());

        // Registra el identificador en el contexto de logging y lo propaga en la respuesta
        MDC.put(TRACE_ID_MDC_KEY, traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);

        // Limpia el contexto al finalizar la petición  y
        // evita que el trace ID se propague a otras peticiones si el hilo es reutilizado
        try {filterChain.doFilter(request, response);}
        finally {MDC.remove(TRACE_ID_MDC_KEY);}
    }
}