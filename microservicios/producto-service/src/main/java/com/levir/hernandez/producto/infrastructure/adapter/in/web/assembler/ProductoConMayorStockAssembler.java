package com.levir.hernandez.producto.infrastructure.adapter.in.web.assembler;

import com.levir.hernandez.producto.application.port.out.ProductoConMayorStock;
import com.levir.hernandez.producto.infrastructure.adapter.in.web.controller.ProductoController;
import com.levir.hernandez.producto.infrastructure.adapter.in.web.dto.response.ProductoConMayorStockResponse;
import com.levir.hernandez.producto.infrastructure.adapter.in.web.mapper.DtoMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProductoConMayorStockAssembler
        implements RepresentationModelAssembler<ProductoConMayorStock, EntityModel<ProductoConMayorStockResponse>>
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

    @Override
    public EntityModel<ProductoConMayorStockResponse> toModel(ProductoConMayorStock producto)
    {
        return EntityModel.of(DtoMapper.toResponse(producto),
                linkTo(methodOn(ProductoController.class).obtener(producto.productoId())).withRel("producto"),
                Link.of(sucursalUrl + "/api/v1/sucursales/" + producto.sucursalId(), "sucursal"));
    }

    public CollectionModel<EntityModel<ProductoConMayorStockResponse>> toCollectionModel(
            List<ProductoConMayorStock> productos, UUID franquiciaId)
    {
        return toCollectionModel(productos)
                .add(linkTo(methodOn(ProductoController.class).obtenerConMayorStock(franquiciaId)).withSelfRel())
                .add(Link.of(franquiciaUrl + "/api/v1/franquicias/" + franquiciaId, "franquicia"));
    }
}
