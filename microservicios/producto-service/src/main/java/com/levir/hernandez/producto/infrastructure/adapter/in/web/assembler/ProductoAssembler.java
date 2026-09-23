package com.levir.hernandez.producto.infrastructure.adapter.in.web.assembler;

import com.levir.hernandez.producto.domain.model.Producto;
import com.levir.hernandez.producto.infrastructure.adapter.in.web.controller.ProductoController;
import com.levir.hernandez.producto.infrastructure.adapter.in.web.dto.response.ProductoResponse;
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

/**
 * En WebFlux los enlaces se construyen de forma asincrona a partir de la peticion en curso
 */
@Component
public class ProductoAssembler
{
    // Las sucursales viven en otro microservicio: se enlazan por su URL publica
    private final String sucursalUrl;

    public ProductoAssembler(@Value("${app.servicios.sucursal-url:http://localhost:8082}") String sucursalUrl)
    {
        this.sucursalUrl = sucursalUrl;
    }

    public Mono<EntityModel<ProductoResponse>> toModel(Producto producto)
    {
        UUID productoId = producto.getId();

        return Flux.concat(
                        linkTo(methodOn(ProductoController.class).obtener(productoId)).withSelfRel().toMono(),
                        Mono.just(sucursal(producto.getSucursalId())),
                        linkTo(methodOn(ProductoController.class).obtener(productoId))
                                .withRel("obtener_producto").toMono(),
                        linkTo(methodOn(ProductoController.class).renombrar(productoId, null))
                                .withRel("renombrar_producto").toMono(),
                        linkTo(methodOn(ProductoController.class).eliminar(productoId))
                                .withRel("eliminar_producto").toMono(),
                        linkTo(methodOn(ProductoController.class).modificarStock(productoId, null))
                                .withRel("modificar_stock_producto").toMono())
                .collectList()
                .map(links -> EntityModel.of(DtoMapper.toResponse(producto), links));
    }

    public Mono<CollectionModel<EntityModel<ProductoResponse>>> toCollectionModel(Flux<Producto> productos,
                                                                                 UUID sucursalId)
    {
        return productos.concatMap(this::toModel)
                .collectList()
                .zipWith(linkTo(methodOn(ProductoController.class).listar(sucursalId)).withSelfRel().toMono())
                .map(modelosYEnlace -> CollectionModel.of(modelosYEnlace.getT1(),
                        modelosYEnlace.getT2(), sucursal(sucursalId)));
    }

    private Link sucursal(UUID sucursalId)
    {
        return Link.of(sucursalUrl + "/api/v1/sucursales/" + sucursalId, "sucursal");
    }
}
