package com.levir.hernandez.producto.infrastructure.adapter.out.messaging;

import com.levir.hernandez.producto.application.exception.ServicioNoDisponibleException;
import com.levir.hernandez.producto.application.port.out.SucursalConsultaPort;
import com.levir.hernandez.producto.application.port.out.SucursalResumen;
import com.levir.hernandez.producto.infrastructure.observability.TraceIdFilter;
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
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

/**
 * Consulta por JMS al servicio de sucursales si una sucursal existe o cuales tiene una franquicia
 * y envia el trace ID actual en el mensaje.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SucursalConsultaJmsAdapter implements SucursalConsultaPort
{
    private static final String SERVICIO = "sucursales";

    private final JmsTemplate jmsTemplate;
    private final JsonMapper jsonMapper;

    @Value("${app.mensajeria.colas.sucursal-existe}")
    private String colaSucursalExiste;

    @Value("${app.mensajeria.colas.sucursales-por-franquicia}")
    private String colaSucursalesPorFranquicia;

    @Override
    @CircuitBreaker(name = "sucursal", fallbackMethod = "existeSucursalFallback")
    public boolean existeSucursal(UUID sucursalId)
    {
        return Boolean.parseBoolean(consultar(colaSucursalExiste, sucursalId));
    }

    @Override
    @CircuitBreaker(name = "sucursal", fallbackMethod = "obtenerSucursalesDeFranquiciaFallback")
    public List<SucursalResumen> obtenerSucursalesDeFranquicia(UUID franquiciaId)
    {
        return jsonMapper.readValue(consultar(colaSucursalesPorFranquicia, franquiciaId), new TypeReference<>() {});
    }

    private boolean existeSucursalFallback(UUID sucursalId, Throwable causa)
    {
        log.warn("No se pudo verificar la sucursal {}: {}", sucursalId, causa.toString());
        throw new ServicioNoDisponibleException(SERVICIO);
    }

    private List<SucursalResumen> obtenerSucursalesDeFranquiciaFallback(UUID franquiciaId, Throwable causa)
    {
        log.warn("No se pudieron obtener las sucursales de la franquicia {}: {}", franquiciaId, causa.toString());
        return List.of();
    }

    private String consultar(String destino, UUID id)
    {
        Message respuesta = jmsTemplate.sendAndReceive(destino, session ->
        {
            TextMessage pregunta = session.createTextMessage(id.toString());
            String traceId = MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY);
            if (traceId != null) pregunta.setStringProperty(TraceIdFilter.TRACE_ID_MDC_KEY, traceId);
            return pregunta;
        });

        if (respuesta == null) throw new ServicioNoDisponibleException(SERVICIO);

        try {return ((TextMessage) respuesta).getText();}
        catch (JMSException e) {throw new ServicioNoDisponibleException(SERVICIO);}
    }
}
