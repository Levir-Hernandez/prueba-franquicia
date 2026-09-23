package com.levir.hernandez.producto.infrastructure.adapter.in.web.assembler;

import com.levir.hernandez.producto.domain.model.Producto;
import com.levir.hernandez.producto.infrastructure.adapter.in.web.controller.ProductoController;
import com.levir.hernandez.producto.infrastructure.adapter.in.web.dto.response.ProductoResponse;
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
public class ProductoAssembler implements RepresentationModelAssembler<Producto, EntityModel<ProductoResponse>>
{
    // Las sucursales viven en otro microservicio: se enlazan por su URL publica
    private final String sucursalUrl;

    public ProductoAssembler(@Value("${app.servicios.sucursal-url:http://localhost:8082}") String sucursalUrl)
    {
        this.sucursalUrl = sucursalUrl;
    }

    @Override
    public EntityModel<ProductoResponse> toModel(Producto producto)
    {
        UUID productoId = producto.getId();

        return EntityModel.of(DtoMapper.toResponse(producto),
                linkTo(methodOn(ProductoController.class).obtener(productoId)).withSelfRel(),
                sucursal(producto.getSucursalId()),
                linkTo(methodOn(ProductoController.class).obtener(productoId)).withRel("obtener_producto"),
                linkTo(methodOn(ProductoController.class).renombrar(productoId, null)).withRel("renombrar_producto"),
                linkTo(methodOn(ProductoController.class).eliminar(productoId)).withRel("eliminar_producto"),
                linkTo(methodOn(ProductoController.class).modificarStock(productoId, null))
                        .withRel("modificar_stock_producto"));
    }

    public CollectionModel<EntityModel<ProductoResponse>> toCollectionModel(List<Producto> productos, UUID sucursalId)
    {
        return toCollectionModel(productos)
                .add(linkTo(methodOn(ProductoController.class).listar(sucursalId)).withSelfRel())
                .add(sucursal(sucursalId));
    }

    private Link sucursal(UUID sucursalId)
    {
        return Link.of(sucursalUrl + "/api/v1/sucursales/" + sucursalId, "sucursal");
    }
}
