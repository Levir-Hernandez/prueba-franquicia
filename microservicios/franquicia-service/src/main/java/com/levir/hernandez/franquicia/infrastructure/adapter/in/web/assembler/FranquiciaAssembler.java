package com.levir.hernandez.franquicia.infrastructure.adapter.in.web.assembler;

import com.levir.hernandez.franquicia.domain.model.Franquicia;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.controller.FranquiciaController;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.dto.response.FranquiciaResponse;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.mapper.DtoMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

/**
 * En WebFlux los enlaces se construyen de forma asincrona a partir de la peticion en curso
 */
@Component
public class FranquiciaAssembler
{
    // Los recursos de otros microservicios no son controladores locales: se enlazan por su URL publica
    private final String sucursalUrl;
    private final String productoUrl;

    public FranquiciaAssembler(@Value("${app.servicios.sucursal-url:http://localhost:8082}") String sucursalUrl,
                               @Value("${app.servicios.producto-url:http://localhost:8083}") String productoUrl)
    {
        this.sucursalUrl = sucursalUrl;
        this.productoUrl = productoUrl;
    }

    public Mono<EntityModel<FranquiciaResponse>> toModel(Franquicia franquicia)
    {
        UUID franquiciaId = franquicia.getId();
        String sucursales = sucursalUrl + "/api/v1/franquicias/" + franquiciaId + "/sucursales";

        return Flux.concat(
                        linkTo(methodOn(FranquiciaController.class).obtener(franquiciaId)).withSelfRel().toMono(),
                        linkTo(methodOn(FranquiciaController.class).listar()).withRel("listar_franquicias").toMono(),
                        linkTo(methodOn(FranquiciaController.class).agregar(null)).withRel("agregar_franquicia").toMono(),
                        linkTo(methodOn(FranquiciaController.class).obtener(franquiciaId))
                                .withRel("obtener_franquicia").toMono(),
                        linkTo(methodOn(FranquiciaController.class).renombrar(franquiciaId, null))
                                .withRel("renombrar_franquicia").toMono(),
                        Mono.just(Link.of(sucursales, "listar_sucursales")),
                        Mono.just(Link.of(sucursales, "agregar_sucursal")),
                        Mono.just(Link.of(productoUrl + "/api/v1/franquicias/" + franquiciaId
                                + "/productos/mayor-stock", "listar_productos_con_mayor_stock")))
                .collectList()
                .map(links -> EntityModel.of(DtoMapper.toResponse(franquicia), links));
    }

    public Mono<CollectionModel<EntityModel<FranquiciaResponse>>> toCollectionModel(Flux<Franquicia> franquicias)
    {
        return franquicias.concatMap(this::toModel)
                .collectList()
                .zipWith(linkTo(methodOn(FranquiciaController.class).listar()).withSelfRel().toMono())
                .map(modelosYEnlace -> CollectionModel.of(modelosYEnlace.getT1(), modelosYEnlace.getT2()));
    }
}
