package com.levir.hernandez.sucursal.infrastructure.observability;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Registra la ejecución de los casos de uso marcados con {@code @ObservableUseCase}
 * a nivel de clase o metodo, incluyendo su duración y resultado
 */
@Slf4j
@Aspect
@Component
public class LogginUseCaseAspect
{
    @Around("@within(com.levir.hernandez.sucursal.application.annotation.ObservableUseCase)"
            + " || @annotation(com.levir.hernandez.sucursal.application.annotation.ObservableUseCase)")
    public Object registrarEjecucion(ProceedingJoinPoint joinPoint) throws Throwable
    {
        String casoDeUso = joinPoint.getSignature().toShortString();
        long inicio = System.currentTimeMillis();

        try
        {
            Object resultado = joinPoint.proceed();
            log.info("Caso de uso {} completado en {} ms", casoDeUso, System.currentTimeMillis() - inicio);
            return resultado;
        }
        catch (Throwable error)
        {
            log.warn("Caso de uso {} fallido en {} ms: {}", casoDeUso, System.currentTimeMillis() - inicio,
                    error.getMessage());
            throw error;
        }
    }
}