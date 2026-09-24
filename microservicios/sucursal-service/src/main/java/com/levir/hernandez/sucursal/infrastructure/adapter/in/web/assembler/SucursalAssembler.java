package com.levir.hernandez.sucursal.infrastructure.adapter.in.web.assembler;

import com.levir.hernandez.sucursal.domain.model.Sucursal;
import com.levir.hernandez.sucursal.infrastructure.adapter.in.web.controller.SucursalController;
import com.levir.hernandez.sucursal.infrastructure.adapter.in.web.dto.response.SucursalResponse;
import com.levir.hernandez.sucursal.infrastructure.adapter.in.web.mapper.DtoMapper;
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
public class SucursalAssembler
{
    // Los recursos de otros microservicios no son controladores locales: se enlazan por su URL publica
    private final String franquiciaUrl;
    private final String productoUrl;

    public SucursalAssembler(@Value("${app.servicios.franquicia-url:http://localhost:8081}") String franquiciaUrl,
                             @Value("${app.servicios.producto-url:http://localhost:8083}") String productoUrl)
    {
        this.franquiciaUrl = franquiciaUrl;
        this.productoUrl = productoUrl;
    }

    public Mono<EntityModel<SucursalResponse>> toModel(Sucursal sucursal)
    {
        UUID sucursalId = sucursal.getId();
        String productos = productoUrl + "/api/v1/sucursales/" + sucursalId + "/productos";

        return Flux.concat(
                        linkTo(methodOn(SucursalController.class).obtener(sucursalId)).withSelfRel().toMono(),
                        Mono.just(franquicia(sucursal.getFranquiciaId())),
                        linkTo(methodOn(SucursalController.class).obtener(sucursalId))
                                .withRel("obtener_sucursal").toMono(),
                        linkTo(methodOn(SucursalController.class).renombrar(sucursalId, null))
                                .withRel("renombrar_sucursal").toMono(),
                        Mono.just(Link.of(productos, "listar_productos")),
                        Mono.just(Link.of(productos, "agregar_producto")))
                .collectList()
                .map(links -> EntityModel.of(DtoMapper.toResponse(sucursal), links));
    }

    public Mono<CollectionModel<EntityModel<SucursalResponse>>> toCollectionModel(Flux<Sucursal> sucursales,
                                                                                 UUID franquiciaId)
    {
        return sucursales.concatMap(this::toModel)
                .collectList()
                .zipWith(linkTo(methodOn(SucursalController.class).listar(franquiciaId)).withSelfRel().toMono())
                .map(modelosYEnlace -> CollectionModel.of(modelosYEnlace.getT1(),
                        modelosYEnlace.getT2(), franquicia(franquiciaId)));
    }

    private Link franquicia(UUID franquiciaId)
    {
        return Link.of(franquiciaUrl + "/api/v1/franquicias/" + franquiciaId, "franquicia");
    }
}
