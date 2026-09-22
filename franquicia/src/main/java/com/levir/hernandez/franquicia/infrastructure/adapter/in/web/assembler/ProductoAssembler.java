package com.levir.hernandez.franquicia.infrastructure.adapter.in.web.assembler;

import com.levir.hernandez.franquicia.domain.model.Producto;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.controller.ProductoController;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.controller.SucursalController;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.dto.response.ProductoResponse;
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
public class ProductoAssembler implements RepresentationModelAssembler<Producto, EntityModel<ProductoResponse>>
{
    @Override
    public EntityModel<ProductoResponse> toModel(Producto producto)
    {
        UUID productoId = producto.getId();

        return EntityModel.of(DtoMapper.toResponse(producto),
                linkTo(methodOn(ProductoController.class).obtener(productoId)).withSelfRel(),
                linkTo(methodOn(SucursalController.class).obtener(producto.getSucursalId())).withRel("sucursal"),
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
                .add(linkTo(methodOn(SucursalController.class).obtener(sucursalId)).withRel("sucursal"));
    }
}
