package com.levir.hernandez.sucursal.infrastructure.adapter.in.web.controller;

import com.levir.hernandez.sucursal.application.exception.FranquiciaNoEncontradaException;
import com.levir.hernandez.sucursal.application.exception.ServicioNoDisponibleException;
import com.levir.hernandez.sucursal.application.exception.SucursalNoEncontradaException;
import com.levir.hernandez.sucursal.application.port.in.sucursal.AgregarSucursalUseCase;
import com.levir.hernandez.sucursal.application.port.in.sucursal.ObtenerSucursalUseCase;
import com.levir.hernandez.sucursal.application.port.in.sucursal.ObtenerSucursalesUseCase;
import com.levir.hernandez.sucursal.application.port.in.sucursal.RenombrarSucursalUseCase;
import com.levir.hernandez.sucursal.domain.model.Sucursal;
import com.levir.hernandez.sucursal.infrastructure.adapter.in.web.assembler.SucursalAssembler;
import com.levir.hernandez.sucursal.infrastructure.config.HateoasConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@WebFluxTest(SucursalController.class)
@Import({SucursalAssembler.class, HateoasConfig.class})
class SucursalControllerTest
{
    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AgregarSucursalUseCase agregarSucursal;
    @MockitoBean
    private ObtenerSucursalUseCase obtenerSucursal;
    @MockitoBean
    private ObtenerSucursalesUseCase obtenerSucursales;
    @MockitoBean
    private RenombrarSucursalUseCase renombrarSucursal;

    private final UUID franquiciaId = UUID.randomUUID();
    private final UUID sucursalId = UUID.randomUUID();

    private Sucursal sucursal(String nombre)
    {
        return new Sucursal(sucursalId, nombre, franquiciaId);
    }

    private WebTestClient.ResponseSpec enviar(WebTestClient.RequestBodyUriSpec metodo, String uri, Object id, String json)
    {
        return metodo.uri(uri, id).contentType(MediaType.APPLICATION_JSON).bodyValue(json).exchange();
    }

    @Test
    @DisplayName("Deberia responder 201 al agregar una sucursal")
    void agregaSucursal()
    {
        when(agregarSucursal.agregarSucursal(franquiciaId, "Burger Express Centro"))
                .thenReturn(Mono.just(sucursal("Burger Express Centro")));

        enviar(webTestClient.post(), "/api/v1/franquicias/{id}/sucursales", franquiciaId,
                "{\"nombre\": \"Burger Express Centro\"}")
                .expectStatus().isCreated()
                .expectHeader().exists("Location")
                .expectBody().jsonPath("$.nombre").isEqualTo("Burger Express Centro");
    }

    @Test
    @DisplayName("Deberia responder 400 al agregar una sucursal sin nombre")
    void noAgregaSucursalSinNombre()
    {
        enviar(webTestClient.post(), "/api/v1/franquicias/{id}/sucursales", franquiciaId, "{\"nombre\": \"\"}")
                .expectStatus().isBadRequest();

        verifyNoInteractions(agregarSucursal);
    }

    @Test
    @DisplayName("Deberia responder 404 al agregar una sucursal a una franquicia que no existe")
    void noAgregaSucursalAFranquiciaInexistente()
    {
        when(agregarSucursal.agregarSucursal(franquiciaId, "Burger Express Centro"))
                .thenReturn(Mono.error(new FranquiciaNoEncontradaException(franquiciaId)));

        enviar(webTestClient.post(), "/api/v1/franquicias/{id}/sucursales", franquiciaId,
                "{\"nombre\": \"Burger Express Centro\"}")
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Deberia responder 503 al agregar una sucursal si el servicio de franquicias no esta disponible")
    void noAgregaSucursalSiServicioNoDisponible()
    {
        when(agregarSucursal.agregarSucursal(franquiciaId, "Burger Express Centro"))
                .thenReturn(Mono.error(new ServicioNoDisponibleException("franquicias")));

        enviar(webTestClient.post(), "/api/v1/franquicias/{id}/sucursales", franquiciaId,
                "{\"nombre\": \"Burger Express Centro\"}")
                .expectStatus().isEqualTo(503);
    }

    @Test
    @DisplayName("Deberia responder 200 al listar las sucursales de una franquicia")
    void listaSucursales()
    {
        when(obtenerSucursales.obtenerSucursales(franquiciaId)).thenReturn(Flux.just(sucursal("Burger Express Centro")));

        webTestClient.get().uri("/api/v1/franquicias/{id}/sucursales", franquiciaId).exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$._embedded.sucursales[0].nombre").isEqualTo("Burger Express Centro");
    }

    @Test
    @DisplayName("Deberia responder 400 al listar sucursales con un id que no es UUID")
    void noListaSucursalesConIdInvalido()
    {
        webTestClient.get().uri("/api/v1/franquicias/{id}/sucursales", "no-es-uuid").exchange()
                .expectStatus().isBadRequest();

        verifyNoInteractions(obtenerSucursales);
    }

    @Test
    @DisplayName("Deberia responder 200 al obtener una sucursal existente")
    void obtieneSucursal()
    {
        when(obtenerSucursal.obtenerSucursal(sucursalId)).thenReturn(Mono.just(sucursal("Burger Express Centro")));

        webTestClient.get().uri("/api/v1/sucursales/{id}", sucursalId).exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.nombre").isEqualTo("Burger Express Centro")
                .jsonPath("$._links.self.href").exists();
    }

    @Test
    @DisplayName("Deberia responder 404 al obtener una sucursal que no existe")
    void noObtieneSucursalInexistente()
    {
        when(obtenerSucursal.obtenerSucursal(sucursalId))
                .thenReturn(Mono.error(new SucursalNoEncontradaException(sucursalId)));

        webTestClient.get().uri("/api/v1/sucursales/{id}", sucursalId).exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Deberia responder 200 al renombrar una sucursal")
    void renombraSucursal()
    {
        when(renombrarSucursal.renombrarSucursal(sucursalId, "Burger Express Poblado"))
                .thenReturn(Mono.just(sucursal("Burger Express Poblado")));

        enviar(webTestClient.patch(), "/api/v1/sucursales/{id}/nombre", sucursalId,
                "{\"nombre\": \"Burger Express Poblado\"}")
                .expectStatus().isOk()
                .expectBody().jsonPath("$.nombre").isEqualTo("Burger Express Poblado");
    }

    @Test
    @DisplayName("Deberia responder 400 al renombrar una sucursal sin nombre")
    void noRenombraSucursalSinNombre()
    {
        enviar(webTestClient.patch(), "/api/v1/sucursales/{id}/nombre", sucursalId, "{\"nombre\": \"\"}")
                .expectStatus().isBadRequest();

        verifyNoInteractions(renombrarSucursal);
    }

    @Test
    @DisplayName("Deberia responder 404 al renombrar una sucursal que no existe")
    void noRenombraSucursalInexistente()
    {
        when(renombrarSucursal.renombrarSucursal(sucursalId, "Burger Express Poblado"))
                .thenReturn(Mono.error(new SucursalNoEncontradaException(sucursalId)));

        enviar(webTestClient.patch(), "/api/v1/sucursales/{id}/nombre", sucursalId,
                "{\"nombre\": \"Burger Express Poblado\"}")
                .expectStatus().isNotFound();
    }
}
