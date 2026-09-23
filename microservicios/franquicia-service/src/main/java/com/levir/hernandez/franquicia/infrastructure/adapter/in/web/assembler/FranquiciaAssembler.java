package com.levir.hernandez.franquicia.infrastructure.adapter.in.web.assembler;

import com.levir.hernandez.franquicia.domain.model.Franquicia;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.controller.FranquiciaController;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.dto.response.FranquiciaResponse;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.mapper.DtoMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class FranquiciaAssembler implements RepresentationModelAssembler<Franquicia, EntityModel<FranquiciaResponse>>
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

    @Override
    public EntityModel<FranquiciaResponse> toModel(Franquicia franquicia)
    {
        UUID franquiciaId = franquicia.getId();
        String sucursales = sucursalUrl + "/api/v1/franquicias/" + franquiciaId + "/sucursales";

        return EntityModel.of(DtoMapper.toResponse(franquicia),
                linkTo(methodOn(FranquiciaController.class).obtener(franquiciaId)).withSelfRel(),
                linkTo(methodOn(FranquiciaController.class).listar()).withRel("listar_franquicias"),
                linkTo(methodOn(FranquiciaController.class).agregar(null)).withRel("agregar_franquicia"),
                linkTo(methodOn(FranquiciaController.class).obtener(franquiciaId)).withRel("obtener_franquicia"),
                linkTo(methodOn(FranquiciaController.class).renombrar(franquiciaId, null)).withRel("renombrar_franquicia"),
                Link.of(sucursales, "listar_sucursales"),
                Link.of(sucursales, "agregar_sucursal"),
                Link.of(productoUrl + "/api/v1/franquicias/" + franquiciaId + "/productos/mayor-stock",
                        "listar_productos_con_mayor_stock"));
    }

    @Override
    public CollectionModel<EntityModel<FranquiciaResponse>> toCollectionModel(Iterable<? extends Franquicia> franquicias)
    {
        return RepresentationModelAssembler.super.toCollectionModel(franquicias)
                .add(linkTo(methodOn(FranquiciaController.class).listar()).withSelfRel());
    }
}
