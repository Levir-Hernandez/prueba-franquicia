package com.levir.hernandez.sucursal.infrastructure.adapter.out.messaging;

import com.levir.hernandez.sucursal.application.exception.ServicioNoDisponibleException;
import com.levir.hernandez.sucursal.application.port.out.FranquiciaConsultaPort;
import com.levir.hernandez.sucursal.infrastructure.observability.TraceIdFilter;
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
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

/**
 * Consulta por JMS al servicio de franquicias si una franquicia existe
 * y envia el trace ID actual en el mensaje.
 * JMS es bloqueante: la consulta se ejecuta en el scheduler boundedElastic para no bloquear el event loop.
 */
@Slf4j
@Component
public class FranquiciaConsultaJmsAdapter implements FranquiciaConsultaPort
{
    private static final String SERVICIO = "franquicias";

    private final JmsTemplate jmsTemplate;
    private final CircuitBreaker circuitBreaker;
    private final String colaFranquiciaExiste;

    public FranquiciaConsultaJmsAdapter(JmsTemplate jmsTemplate, CircuitBreakerRegistry circuitBreakerRegistry,
                                        @Value("${app.mensajeria.colas.franquicia-existe}") String colaFranquiciaExiste)
    {
        this.jmsTemplate = jmsTemplate;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("franquicia");
        this.colaFranquiciaExiste = colaFranquiciaExiste;
    }

    @Override
    public Mono<Boolean> existeFranquicia(UUID franquiciaId)
    {
        // El traceId se toma del contexto de Reactor
        return Mono.deferContextual(contexto ->
                {
                    String traceId = contexto.getOrDefault(TraceIdFilter.TRACE_ID_MDC_KEY, null);
                    return Mono.fromCallable(() -> enviarYRecibir(franquiciaId, traceId))
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .map(Boolean::parseBoolean)
                .onErrorResume(causa ->
                {
                    log.warn("No se pudo verificar la franquicia {}: {}", franquiciaId, causa.toString());
                    return Mono.error(new ServicioNoDisponibleException(SERVICIO));
                });
    }

    private String enviarYRecibir(UUID franquiciaId, String traceId) throws JMSException
    {
        Message respuesta = jmsTemplate.sendAndReceive(colaFranquiciaExiste, session ->
        {
            TextMessage pregunta = session.createTextMessage(franquiciaId.toString());
            if (traceId != null) pregunta.setStringProperty(TraceIdFilter.TRACE_ID_MDC_KEY, traceId);
            return pregunta;
        });

        if (respuesta == null) throw new ServicioNoDisponibleException(SERVICIO);
        return ((TextMessage) respuesta).getText();
    }
}
