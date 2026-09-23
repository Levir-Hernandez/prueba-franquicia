package com.levir.hernandez.franquicia.infrastructure.adapter.in.web.assembler;

import com.levir.hernandez.franquicia.domain.model.Franquicia;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.controller.FranquiciaController;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.controller.ProductoController;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.controller.SucursalController;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.dto.response.FranquiciaResponse;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.mapper.DtoMapper;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class FranquiciaAssembler implements RepresentationModelAssembler<Franquicia, EntityModel<FranquiciaResponse>>
{
    @Override
    public EntityModel<FranquiciaResponse> toModel(Franquicia franquicia)
    {
        UUID franquiciaId = franquicia.getId();

        return EntityModel.of(DtoMapper.toResponse(franquicia),
                linkTo(methodOn(FranquiciaController.class).obtener(franquiciaId)).withSelfRel(),
                linkTo(methodOn(FranquiciaController.class).listar()).withRel("listar_franquicias"),
                linkTo(methodOn(FranquiciaController.class).agregar(null)).withRel("agregar_franquicia"),
                linkTo(methodOn(FranquiciaController.class).obtener(franquiciaId)).withRel("obtener_franquicia"),
                linkTo(methodOn(FranquiciaController.class).renombrar(franquiciaId, null)).withRel("renombrar_franquicia"),
                linkTo(methodOn(SucursalController.class).listar(franquiciaId)).withRel("listar_sucursales"),
                linkTo(methodOn(SucursalController.class).agregar(franquiciaId, null)).withRel("agregar_sucursal"),
                linkTo(methodOn(ProductoController.class).obtenerConMayorStock(franquiciaId))
                        .withRel("listar_productos_con_mayor_stock"));
    }

    @Override
    public CollectionModel<EntityModel<FranquiciaResponse>> toCollectionModel(Iterable<? extends Franquicia> franquicias)
    {
        return RepresentationModelAssembler.super.toCollectionModel(franquicias)
                .add(linkTo(methodOn(FranquiciaController.class).listar()).withSelfRel());
    }
}
