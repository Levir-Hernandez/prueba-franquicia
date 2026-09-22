package com.levir.hernandez.franquicia.infrastructure.adapter.in.web.controller;

import com.levir.hernandez.franquicia.application.port.in.ObtenerProductosConMayorStockUseCase;
import com.levir.hernandez.franquicia.application.port.in.producto.*;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.assembler.ProductoAssembler;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.assembler.ProductoConMayorStockAssembler;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.dto.request.AgregarProductoRequest;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.dto.request.ModificarStockRequest;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.dto.request.NombreRecursoRequest;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.dto.response.ProductoConMayorStockResponse;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.dto.response.ProductoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Productos")
public class ProductoController
{
    private final AgregarProductoUseCase agregarProducto;
    private final ObtenerProductoUseCase obtenerProducto;
    private final ObtenerProductosUseCase obtenerProductos;
    private final EliminarProductoUseCase eliminarProducto;
    private final ModificarStockProductoUseCase modificarStockProducto;
    private final RenombrarProductoUseCase renombrarProducto;
    private final ObtenerProductosConMayorStockUseCase obtenerProductosConMayorStock;
    private final ProductoAssembler assembler;
    private final ProductoConMayorStockAssembler mayorStockAssembler;

    // AgregarProductoUseCase
    @Operation(
            summary = "Agrega un producto a una sucursal",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nombre y stock inicial del producto", required = true,
                    content = @Content(schema = @Schema(implementation = AgregarProductoRequest.class))
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201", description = "Producto creado correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Id, nombre o stock invalido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Sucursal no encontrada", content = @Content)
    })
    @PostMapping("/sucursales/{sucursalId}/productos")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<EntityModel<ProductoResponse>> agregar(
            @Parameter(description = "Id de la sucursal", required = true)
            @PathVariable UUID sucursalId,

            @Valid @RequestBody AgregarProductoRequest request)
    {
        EntityModel<ProductoResponse> body = assembler.toModel(
                agregarProducto.agregarProducto(sucursalId, request.nombre(), request.stock()));
        return ResponseEntity.created(body.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(body);
    }

    // ObtenerProductosUseCase
    @Operation(summary = "Lista los productos de una sucursal")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Productos obtenidos correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ProductoResponse.class))
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Id invalido", content = @Content)
    })
    @GetMapping("/sucursales/{sucursalId}/productos")
    @ResponseStatus(HttpStatus.OK)
    public CollectionModel<EntityModel<ProductoResponse>> listar(
            @Parameter(description = "Id de la sucursal", required = true)
            @PathVariable UUID sucursalId)
    {
        return assembler.toCollectionModel(obtenerProductos.obtenerProductos(sucursalId), sucursalId);
    }

    // ObtenerProductoUseCase
    @Operation(summary = "Obtiene un producto")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Producto obtenido correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Id invalido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
    })
    @GetMapping("/productos/{productoId}")
    @ResponseStatus(HttpStatus.OK)
    public EntityModel<ProductoResponse> obtener(
            @Parameter(description = "Id del producto", required = true)
            @PathVariable UUID productoId)
    {
        return assembler.toModel(obtenerProducto.obtenerProducto(productoId));
    }

    // EliminarProductoUseCase
    @Operation(summary = "Elimina un producto")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto eliminado correctamente", content = @Content),
            @ApiResponse(responseCode = "400", description = "Id invalido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
    })
    @DeleteMapping("/productos/{productoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "Id del producto", required = true)
            @PathVariable UUID productoId)
    {
        eliminarProducto.eliminarProducto(productoId);
        return ResponseEntity.noContent().build();
    }

    // ModificarStockProductoUseCase
    @Operation(
            summary = "Modifica el stock de un producto",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nuevo stock del producto", required = true,
                    content = @Content(schema = @Schema(implementation = ModificarStockRequest.class))
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Stock modificado correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Id o stock invalido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
    })
    @PatchMapping("/productos/{productoId}/stock")
    @ResponseStatus(HttpStatus.OK)
    public EntityModel<ProductoResponse> modificarStock(
            @Parameter(description = "Id del producto", required = true)
            @PathVariable UUID productoId,

            @Valid @RequestBody ModificarStockRequest request)
    {
        return assembler.toModel(modificarStockProducto.modificarStockProducto(productoId, request.stock()));
    }

    // RenombrarProductoUseCase
    @Operation(
            summary = "Renombra un producto",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nuevo nombre del producto", required = true,
                    content = @Content(schema = @Schema(implementation = NombreRecursoRequest.class))
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Producto renombrado correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Id o nombre invalido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
    })
    @PatchMapping("/productos/{productoId}/nombre")
    @ResponseStatus(HttpStatus.OK)
    public EntityModel<ProductoResponse> renombrar(
            @Parameter(description = "Id del producto", required = true)
            @PathVariable UUID productoId,

            @Valid @RequestBody NombreRecursoRequest request)
    {
        return assembler.toModel(renombrarProducto.renombrarProducto(productoId, request.nombre()));
    }

    // ObtenerProductosConMayorStockUseCase
    @Operation(summary = "Lista el producto con mas stock de cada sucursal de una franquicia")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Productos obtenidos correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ProductoConMayorStockResponse.class))
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Id invalido", content = @Content)
    })
    @GetMapping("/franquicias/{franquiciaId}/productos/mayor-stock")
    @ResponseStatus(HttpStatus.OK)
    public CollectionModel<EntityModel<ProductoConMayorStockResponse>> obtenerConMayorStock(
            @Parameter(description = "Id de la franquicia", required = true)
            @PathVariable UUID franquiciaId)
    {
        return mayorStockAssembler.toCollectionModel(
                obtenerProductosConMayorStock.obtenerProductosConMayorStock(franquiciaId), franquiciaId);
    }
}
