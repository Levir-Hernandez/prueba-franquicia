package com.levir.hernandez.sucursal.infrastructure.adapter.out.messaging;

import com.levir.hernandez.sucursal.application.exception.ServicioNoDisponibleException;
import com.levir.hernandez.sucursal.application.port.out.FranquiciaConsultaPort;
import com.levir.hernandez.sucursal.infrastructure.observability.TraceIdFilter;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Consulta por JMS al servicio de franquicias si una franquicia existe
 * y envia el trace ID actual en el mensaje.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FranquiciaConsultaJmsAdapter implements FranquiciaConsultaPort
{
    private static final String SERVICIO = "franquicias";

    private final JmsTemplate jmsTemplate;

    @Value("${app.mensajeria.colas.franquicia-existe}")
    private String colaFranquiciaExiste;

    @Override
    @CircuitBreaker(name = "franquicia", fallbackMethod = "existeFranquiciaFallback")
    public boolean existeFranquicia(UUID franquiciaId)
    {
        Message respuesta = jmsTemplate.sendAndReceive(colaFranquiciaExiste, session ->
        {
            TextMessage pregunta = session.createTextMessage(franquiciaId.toString());
            String traceId = MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY);
            if (traceId != null) pregunta.setStringProperty(TraceIdFilter.TRACE_ID_MDC_KEY, traceId);
            return pregunta;
        });

        if (respuesta == null) throw new ServicioNoDisponibleException(SERVICIO);

        try {return Boolean.parseBoolean(((TextMessage) respuesta).getText());}
        catch (JMSException e) {throw new ServicioNoDisponibleException(SERVICIO);}
    }

    private boolean existeFranquiciaFallback(UUID franquiciaId, Throwable causa)
    {
        log.warn("No se pudo verificar la franquicia {}: {}", franquiciaId, causa.toString());
        throw new ServicioNoDisponibleException(SERVICIO);
    }
}
