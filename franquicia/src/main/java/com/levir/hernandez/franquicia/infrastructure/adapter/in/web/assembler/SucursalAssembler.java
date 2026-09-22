package com.levir.hernandez.franquicia.infrastructure.adapter.in.web.assembler;

import com.levir.hernandez.franquicia.domain.model.Sucursal;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.controller.FranquiciaController;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.controller.ProductoController;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.controller.SucursalController;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.dto.response.SucursalResponse;
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
public class SucursalAssembler implements RepresentationModelAssembler<Sucursal, EntityModel<SucursalResponse>>
{
    @Override
    public EntityModel<SucursalResponse> toModel(Sucursal sucursal)
    {
        UUID sucursalId = sucursal.getId();

        return EntityModel.of(DtoMapper.toResponse(sucursal),
                linkTo(methodOn(SucursalController.class).obtener(sucursalId)).withSelfRel(),
                linkTo(methodOn(FranquiciaController.class).obtener(sucursal.getFranquiciaId())).withRel("franquicia"),
                linkTo(methodOn(SucursalController.class).obtener(sucursalId)).withRel("obtener_sucursal"),
                linkTo(methodOn(SucursalController.class).renombrar(sucursalId, null)).withRel("renombrar_sucursal"),
                linkTo(methodOn(ProductoController.class).listar(sucursalId)).withRel("listar_productos"),
                linkTo(methodOn(ProductoController.class).agregar(sucursalId, null)).withRel("agregar_producto"));
    }

    public CollectionModel<EntityModel<SucursalResponse>> toCollectionModel(List<Sucursal> sucursales, UUID franquiciaId)
    {
        return toCollectionModel(sucursales)
                .add(linkTo(methodOn(SucursalController.class).listar(franquiciaId)).withSelfRel())
                .add(linkTo(methodOn(FranquiciaController.class).obtener(franquiciaId)).withRel("franquicia"));
    }
}
