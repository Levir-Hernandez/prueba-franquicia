package com.levir.hernandez.franquicia.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;
import tools.jackson.databind.module.SimpleModule;

/**
 * Mantiene los enlaces de Spring HATEOAS al final de la representación.
 */
@Component
public class EntityModelJacksonModule extends SimpleModule
{
    public EntityModelJacksonModule()
    {
        setMixInAnnotation(EntityModel.class, EntityModelMixin.class);
    }

    @JsonPropertyOrder({"content", "links"})
    abstract static class EntityModelMixin {}
}