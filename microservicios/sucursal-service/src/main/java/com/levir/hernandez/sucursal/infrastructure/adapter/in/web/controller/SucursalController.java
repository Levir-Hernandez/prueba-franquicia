package com.levir.hernandez.sucursal.infrastructure.adapter.in.web.controller;

import com.levir.hernandez.sucursal.application.port.in.sucursal.AgregarSucursalUseCase;
import com.levir.hernandez.sucursal.application.port.in.sucursal.ObtenerSucursalUseCase;
import com.levir.hernandez.sucursal.application.port.in.sucursal.ObtenerSucursalesUseCase;
import com.levir.hernandez.sucursal.application.port.in.sucursal.RenombrarSucursalUseCase;
import com.levir.hernandez.sucursal.infrastructure.adapter.in.web.assembler.SucursalAssembler;
import com.levir.hernandez.sucursal.infrastructure.adapter.in.web.dto.request.NombreRecursoRequest;
import com.levir.hernandez.sucursal.infrastructure.adapter.in.web.dto.response.SucursalResponse;
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
@Tag(name = "Sucursales")
public class SucursalController
{
    private final AgregarSucursalUseCase agregarSucursal;
    private final ObtenerSucursalUseCase obtenerSucursal;
    private final ObtenerSucursalesUseCase obtenerSucursales;
    private final RenombrarSucursalUseCase renombrarSucursal;
    private final SucursalAssembler assembler;

    // AgregarSucursalUseCase
    @Operation(
            summary = "Agrega una sucursal a una franquicia",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nombre de la sucursal", required = true,
                    content = @Content(schema = @Schema(implementation = NombreRecursoRequest.class))
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201", description = "Sucursal creada correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SucursalResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Id o nombre invalido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Franquicia no encontrada", content = @Content),
            @ApiResponse(responseCode = "503", description = "El servicio de franquicias no esta disponible", content = @Content)
    })
    @PostMapping("/franquicias/{franquiciaId}/sucursales")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<EntityModel<SucursalResponse>> agregar(
            @Parameter(description = "Id de la franquicia", required = true)
            @PathVariable UUID franquiciaId,

            @Valid @RequestBody NombreRecursoRequest request)
    {
        EntityModel<SucursalResponse> body = assembler.toModel(
                agregarSucursal.agregarSucursal(franquiciaId, request.nombre()));
        return ResponseEntity.created(body.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(body);
    }

    // ObtenerSucursalesUseCase
    @Operation(summary = "Lista las sucursales de una franquicia")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Sucursales obtenidas correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = SucursalResponse.class))
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Id invalido", content = @Content)
    })
    @GetMapping("/franquicias/{franquiciaId}/sucursales")
    @ResponseStatus(HttpStatus.OK)
    public CollectionModel<EntityModel<SucursalResponse>> listar(
            @Parameter(description = "Id de la franquicia", required = true)
            @PathVariable UUID franquiciaId)
    {
        return assembler.toCollectionModel(obtenerSucursales.obtenerSucursales(franquiciaId), franquiciaId);
    }

    // ObtenerSucursalUseCase
    @Operation(summary = "Obtiene una sucursal")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Sucursal obtenida correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SucursalResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Id invalido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Sucursal no encontrada", content = @Content)
    })
    @GetMapping("/sucursales/{sucursalId}")
    @ResponseStatus(HttpStatus.OK)
    public EntityModel<SucursalResponse> obtener(
            @Parameter(description = "Id de la sucursal", required = true)
            @PathVariable UUID sucursalId)
    {
        return assembler.toModel(obtenerSucursal.obtenerSucursal(sucursalId));
    }

    // RenombrarSucursalUseCase
    @Operation(
            summary = "Renombra una sucursal",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nuevo nombre de la sucursal", required = true,
                    content = @Content(schema = @Schema(implementation = NombreRecursoRequest.class))
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Sucursal renombrada correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SucursalResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Id o nombre invalido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Sucursal no encontrada", content = @Content)
    })
    @PatchMapping("/sucursales/{sucursalId}/nombre")
    @ResponseStatus(HttpStatus.OK)
    public EntityModel<SucursalResponse> renombrar(
            @Parameter(description = "Id de la sucursal", required = true)
            @PathVariable UUID sucursalId,

            @Valid @RequestBody NombreRecursoRequest request)
    {
        return assembler.toModel(renombrarSucursal.renombrarSucursal(sucursalId, request.nombre()));
    }
}
