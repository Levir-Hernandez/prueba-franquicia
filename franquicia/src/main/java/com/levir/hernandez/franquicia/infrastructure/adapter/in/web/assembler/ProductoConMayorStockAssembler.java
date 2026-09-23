package com.levir.hernandez.franquicia.infrastructure.adapter.in.web.assembler;

import com.levir.hernandez.franquicia.application.port.out.ProductoConMayorStock;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.controller.FranquiciaController;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.controller.ProductoController;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.controller.SucursalController;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.dto.response.ProductoConMayorStockResponse;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.mapper.DtoMapper;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
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
    @Override
    public EntityModel<ProductoConMayorStockResponse> toModel(ProductoConMayorStock producto)
    {
        return EntityModel.of(DtoMapper.toResponse(producto),
                linkTo(methodOn(ProductoController.class).obtener(producto.productoId())).withRel("producto"),
                linkTo(methodOn(SucursalController.class).obtener(producto.sucursalId())).withRel("sucursal"));
    }

    public CollectionModel<EntityModel<ProductoConMayorStockResponse>> toCollectionModel(
            List<ProductoConMayorStock> productos, UUID franquiciaId)
    {
        return toCollectionModel(productos)
                .add(linkTo(methodOn(ProductoController.class).obtenerConMayorStock(franquiciaId)).withSelfRel())
                .add(linkTo(methodOn(FranquiciaController.class).obtener(franquiciaId)).withRel("franquicia"));
    }
}
