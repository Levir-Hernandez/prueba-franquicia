package com.levir.hernandez.franquicia.infrastructure.adapter.in.web.controller;

import com.levir.hernandez.franquicia.application.port.in.franquicia.AgregarFranquiciaUseCase;
import com.levir.hernandez.franquicia.application.port.in.franquicia.ObtenerFranquiciaUseCase;
import com.levir.hernandez.franquicia.application.port.in.franquicia.ObtenerFranquiciasUseCase;
import com.levir.hernandez.franquicia.application.port.in.franquicia.RenombrarFranquiciaUseCase;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.assembler.FranquiciaAssembler;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.dto.request.NombreRecursoRequest;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.dto.response.FranquiciaResponse;
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
@RequestMapping("/api/v1/franquicias")
@RequiredArgsConstructor
@Tag(name = "Franquicias")
public class FranquiciaController
{
    private final AgregarFranquiciaUseCase agregarFranquicia;
    private final ObtenerFranquiciaUseCase obtenerFranquicia;
    private final ObtenerFranquiciasUseCase obtenerFranquicias;
    private final RenombrarFranquiciaUseCase renombrarFranquicia;
    private final FranquiciaAssembler assembler;

    // AgregarFranquiciaUseCase
    @Operation(
            summary = "Agrega una franquicia",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nombre de la franquicia", required = true,
                    content = @Content(schema = @Schema(implementation = NombreRecursoRequest.class))
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201", description = "Franquicia creada correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FranquiciaResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Nombre invalido", content = @Content)
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<EntityModel<FranquiciaResponse>> agregar(@Valid @RequestBody NombreRecursoRequest request)
    {
        EntityModel<FranquiciaResponse> body = assembler.toModel(agregarFranquicia.agregarFranquicia(request.nombre()));
        return ResponseEntity.created(body.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(body);
    }

    // ObtenerFranquiciasUseCase
    @Operation(summary = "Lista las franquicias")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Franquicias obtenidas correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = FranquiciaResponse.class))
                    )
            )
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public CollectionModel<EntityModel<FranquiciaResponse>> listar()
    {
        return assembler.toCollectionModel(obtenerFranquicias.obtenerFranquicias());
    }

    // ObtenerFranquiciaUseCase
    @Operation(summary = "Obtiene una franquicia")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Franquicia obtenida correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FranquiciaResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Id invalido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Franquicia no encontrada", content = @Content)
    })
    @GetMapping("/{franquiciaId}")
    @ResponseStatus(HttpStatus.OK)
    public EntityModel<FranquiciaResponse> obtener(
            @Parameter(description = "Id de la franquicia", required = true)
            @PathVariable UUID franquiciaId)
    {
        return assembler.toModel(obtenerFranquicia.obtenerFranquicia(franquiciaId));
    }

    // RenombrarFranquiciaUseCase
    @Operation(
            summary = "Renombra una franquicia",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nuevo nombre de la franquicia", required = true,
                    content = @Content(schema = @Schema(implementation = NombreRecursoRequest.class))
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Franquicia renombrada correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FranquiciaResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Id o nombre invalido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Franquicia no encontrada", content = @Content)
    })
    @PatchMapping("/{franquiciaId}/nombre")
    @ResponseStatus(HttpStatus.OK)
    public EntityModel<FranquiciaResponse> renombrar(
            @Parameter(description = "Id de la franquicia", required = true)
            @PathVariable UUID franquiciaId,

            @Valid @RequestBody NombreRecursoRequest request)
    {
        return assembler.toModel(renombrarFranquicia.renombrarFranquicia(franquiciaId, request.nombre()));
    }
}
