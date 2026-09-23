package com.levir.hernandez.producto.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.support.WebStack;

/** Respuestas HAL y construccion de enlaces sobre WebFlux */
@Configuration
@EnableHypermediaSupport(type = HypermediaType.HAL, stacks = WebStack.WEBFLUX)
public class HateoasConfig
{
}
