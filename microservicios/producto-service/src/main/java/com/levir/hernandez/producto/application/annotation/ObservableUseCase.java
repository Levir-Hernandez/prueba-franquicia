package com.levir.hernandez.producto.application.annotation;

import java.lang.annotation.*;

/**
 * Anotación que marca casos de uso cuya ejecucion puede ser interceptada por aspectos
 * de infraestructura para fines de trazabilidad y registro
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ObservableUseCase {}
