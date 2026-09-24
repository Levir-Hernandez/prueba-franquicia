package com.levir.hernandez.producto.infrastructure.adapter.out.messaging;

import com.levir.hernandez.producto.application.exception.ServicioNoDisponibleException;
import com.levir.hernandez.producto.application.port.out.SucursalConsultaPort;
import com.levir.hernandez.producto.application.port.out.SucursalResumen;
import com.levir.hernandez.producto.infrastructure.observability.TraceIdFilter;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

/**
 * Consulta por JMS al servicio de sucursales si una sucursal existe o cuales tiene una franquicia
 * y envia el trace ID actual en el mensaje.
 * JMS es bloqueante: cada consulta se ejecuta en el scheduler boundedElastic para no bloquear el event loop.
 */
@Slf4j
@Component
public class SucursalConsultaJmsAdapter implements SucursalConsultaPort
{
    private static final String SERVICIO = "sucursales";

    private final JmsTemplate jmsTemplate;
    private final JsonMapper jsonMapper;
    private final CircuitBreaker circuitBreaker;
    private final String colaSucursalExiste;
    private final String colaSucursalesPorFranquicia;

    public SucursalConsultaJmsAdapter(JmsTemplate jmsTemplate, JsonMapper jsonMapper,
                                      CircuitBreakerRegistry circuitBreakerRegistry,
                                      @Value("${app.mensajeria.colas.sucursal-existe}") String colaSucursalExiste,
                                      @Value("${app.mensajeria.colas.sucursales-por-franquicia}")
                                      String colaSucursalesPorFranquicia)
    {
        this.jmsTemplate = jmsTemplate;
        this.jsonMapper = jsonMapper;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("sucursal");
        this.colaSucursalExiste = colaSucursalExiste;
        this.colaSucursalesPorFranquicia = colaSucursalesPorFranquicia;
    }

    @Override
    public Mono<Boolean> existeSucursal(UUID sucursalId)
    {
        return consultar(colaSucursalExiste, sucursalId)
                .map(Boolean::parseBoolean)
                .onErrorResume(causa ->
                {
                    log.warn("No se pudo verificar la sucursal {}: {}", sucursalId, causa.toString());
                    return Mono.error(new ServicioNoDisponibleException(SERVICIO));
                });
    }

    @Override
    public Flux<SucursalResumen> obtenerSucursalesDeFranquicia(UUID franquiciaId)
    {
        return consultar(colaSucursalesPorFranquicia, franquiciaId)
                .map(json -> jsonMapper.readValue(json, new TypeReference<List<SucursalResumen>>() {}))
                .flatMapIterable(sucursales -> sucursales)
                .onErrorResume(causa ->
                {
                    log.warn("No se pudieron obtener las sucursales de la franquicia {}: {}", franquiciaId,
                            causa.toString());
                    return Flux.empty();
                });
    }

    /** Pregunta request-reply protegida por el circuit breaker; el traceId se toma del contexto de Reactor. */
    private Mono<String> consultar(String destino, UUID id)
    {
        return Mono.deferContextual(contexto ->
                {
                    String traceId = contexto.getOrDefault(TraceIdFilter.TRACE_ID_MDC_KEY, null);
                    return Mono.fromCallable(() -> enviarYRecibir(destino, id, traceId))
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker));
    }

    private String enviarYRecibir(String destino, UUID id, String traceId) throws JMSException
    {
        Message respuesta = jmsTemplate.sendAndReceive(destino, session ->
        {
            TextMessage pregunta = session.createTextMessage(id.toString());
            if (traceId != null) pregunta.setStringProperty(TraceIdFilter.TRACE_ID_MDC_KEY, traceId);
            return pregunta;
        });

        if (respuesta == null) throw new ServicioNoDisponibleException(SERVICIO);
        return ((TextMessage) respuesta).getText();
    }
}
