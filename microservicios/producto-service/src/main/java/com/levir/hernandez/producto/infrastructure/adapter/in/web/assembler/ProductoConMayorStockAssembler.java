package com.levir.hernandez.producto.infrastructure.adapter.in.web.assembler;

import com.levir.hernandez.producto.application.port.out.ProductoConMayorStock;
import com.levir.hernandez.producto.infrastructure.adapter.in.web.controller.ProductoController;
import com.levir.hernandez.producto.infrastructure.adapter.in.web.dto.response.ProductoConMayorStockResponse;
import com.levir.hernandez.producto.infrastructure.adapter.in.web.mapper.DtoMapper;
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

@Component
public class ProductoConMayorStockAssembler
{
    // Las franquicias y sucursales viven en otros microservicios: se enlazan por su URL publica
    private final String franquiciaUrl;
    private final String sucursalUrl;

    public ProductoConMayorStockAssembler(
            @Value("${app.servicios.franquicia-url:http://localhost:8081}") String franquiciaUrl,
            @Value("${app.servicios.sucursal-url:http://localhost:8082}") String sucursalUrl)
    {
        this.franquiciaUrl = franquiciaUrl;
        this.sucursalUrl = sucursalUrl;
    }

    public Mono<EntityModel<ProductoConMayorStockResponse>> toModel(ProductoConMayorStock producto)
    {
        return linkTo(methodOn(ProductoController.class).obtener(producto.productoId())).withRel("producto").toMono()
                .map(enlaceProducto -> EntityModel.of(DtoMapper.toResponse(producto), enlaceProducto,
                        Link.of(sucursalUrl + "/api/v1/sucursales/" + producto.sucursalId(), "sucursal")));
    }

    public Mono<CollectionModel<EntityModel<ProductoConMayorStockResponse>>> toCollectionModel(
            Flux<ProductoConMayorStock> productos, UUID franquiciaId)
    {
        return productos.concatMap(this::toModel)
                .collectList()
                .zipWith(linkTo(methodOn(ProductoController.class).obtenerConMayorStock(franquiciaId))
                        .withSelfRel().toMono())
                .map(modelosYEnlace -> CollectionModel.of(modelosYEnlace.getT1(), modelosYEnlace.getT2(),
                        Link.of(franquiciaUrl + "/api/v1/franquicias/" + franquiciaId, "franquicia")));
    }
}
