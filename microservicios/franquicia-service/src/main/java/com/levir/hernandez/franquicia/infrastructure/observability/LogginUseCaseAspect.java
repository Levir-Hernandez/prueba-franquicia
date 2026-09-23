package com.levir.hernandez.franquicia.infrastructure.observability;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Registra la ejecución de los casos de uso marcados con {@code @ObservableUseCase}
 * a nivel de clase o metodo, incluyendo su duración y resultado.
 * Los casos de uso son reactivos: la duracion se mide desde la suscripcion hasta que el flujo termina.
 */
@Slf4j
@Aspect
@Component
public class LogginUseCaseAspect
{
    @Around("@within(com.levir.hernandez.franquicia.application.annotation.ObservableUseCase)"
            + " || @annotation(com.levir.hernandez.franquicia.application.annotation.ObservableUseCase)")
    public Object registrarEjecucion(ProceedingJoinPoint joinPoint) throws Throwable
    {
        String casoDeUso = joinPoint.getSignature().toShortString();
        Object resultado = joinPoint.proceed();

        if (resultado instanceof Mono<?> mono)
        {
            return Mono.defer(() ->
            {
                long inicio = System.currentTimeMillis();
                return mono.doOnSuccess(valor -> completado(casoDeUso, inicio))
                        .doOnError(error -> fallido(casoDeUso, inicio, error));
            });
        }

        if (resultado instanceof Flux<?> flux)
        {
            return Flux.defer(() ->
            {
                long inicio = System.currentTimeMillis();
                return flux.doOnComplete(() -> completado(casoDeUso, inicio))
                        .doOnError(error -> fallido(casoDeUso, inicio, error));
            });
        }

        return resultado;
    }

    private void completado(String casoDeUso, long inicio)
    {
        log.info("Caso de uso {} completado en {} ms", casoDeUso, System.currentTimeMillis() - inicio);
    }

    private void fallido(String casoDeUso, long inicio, Throwable error)
    {
        log.warn("Caso de uso {} fallido en {} ms: {}", casoDeUso, System.currentTimeMillis() - inicio,
                error.getMessage());
    }
}
