package com.levir.hernandez.sucursal.infrastructure.adapter.in.web.assembler;

import com.levir.hernandez.sucursal.domain.model.Sucursal;
import com.levir.hernandez.sucursal.infrastructure.adapter.in.web.controller.SucursalController;
import com.levir.hernandez.sucursal.infrastructure.adapter.in.web.dto.response.SucursalResponse;
import com.levir.hernandez.sucursal.infrastructure.adapter.in.web.mapper.DtoMapper;
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
public class SucursalAssembler implements RepresentationModelAssembler<Sucursal, EntityModel<SucursalResponse>>
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

    @Override
    public EntityModel<SucursalResponse> toModel(Sucursal sucursal)
    {
        UUID sucursalId = sucursal.getId();
        String productos = productoUrl + "/api/v1/sucursales/" + sucursalId + "/productos";

        return EntityModel.of(DtoMapper.toResponse(sucursal),
                linkTo(methodOn(SucursalController.class).obtener(sucursalId)).withSelfRel(),
                franquicia(sucursal.getFranquiciaId()),
                linkTo(methodOn(SucursalController.class).obtener(sucursalId)).withRel("obtener_sucursal"),
                linkTo(methodOn(SucursalController.class).renombrar(sucursalId, null)).withRel("renombrar_sucursal"),
                Link.of(productos, "listar_productos"),
                Link.of(productos, "agregar_producto"));
    }

    public CollectionModel<EntityModel<SucursalResponse>> toCollectionModel(List<Sucursal> sucursales, UUID franquiciaId)
    {
        return toCollectionModel(sucursales)
                .add(linkTo(methodOn(SucursalController.class).listar(franquiciaId)).withSelfRel())
                .add(franquicia(franquiciaId));
    }

    private Link franquicia(UUID franquiciaId)
    {
        return Link.of(franquiciaUrl + "/api/v1/franquicias/" + franquiciaId, "franquicia");
    }
}
