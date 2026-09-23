package com.levir.hernandez.producto.infrastructure.config;

import com.levir.hernandez.producto.infrastructure.adapter.in.web.dto.response.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/** Documentacion publicada en /swagger-ui.html */
@Configuration
public class OpenApiConfig
{
    private static final String ESQUEMA_ERROR = ErrorResponse.class.getSimpleName();

    @Bean
    public OpenAPI openApi()
    {
        return new OpenAPI()
                .info(new Info()
                        .title("Producto API")
                        .version("v1")
                        .description("Microservicio para gestionar los productos de cada sucursal y su stock. "
                                + "Las respuestas incluyen enlaces HATEOAS y todos los errores comparten un formato estandar.")
                        .contact(new Contact()
                                .name("Levir Hernandez")
                                .url("https://www.linkedin.com/in/levir-heladio-hernandez-suarez-6916a6254")
                        )
                )
                .tags(List.of(new Tag().name("Productos").description("Productos de una sucursal y su stock")))
                .components(new Components());
    }

    /**
     * Los controladores declaran sus errores sin cuerpo para no repetirlo: aqui se les asigna ErrorResponse,
     * que es lo que devuelve el manejador global, y se anade el 500 a cada operacion.
     */
    @Bean
    public GlobalOpenApiCustomizer respuestasDeErrorCustomizer()
    {
        return openApi ->
        {
            ModelConverters.getInstance().read(ErrorResponse.class).forEach(openApi.getComponents()::addSchemas);

            openApi.getPaths().values().forEach(path -> path.readOperations().forEach(operation ->
            {
                operation.getResponses().addApiResponse("500", new ApiResponse().description("Error inesperado"));

                operation.getResponses().forEach((codigo, respuesta) ->
                {
                    if (codigo.startsWith("4") || codigo.startsWith("5"))
                    {
                        respuesta.setContent(cuerpoDeError());
                    }
                });
            }));
        };
    }

    private Content cuerpoDeError()
    {
        return new Content().addMediaType(
                org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                new MediaType().schema(new Schema<>().$ref("#/components/schemas/" + ESQUEMA_ERROR)));
    }
}
