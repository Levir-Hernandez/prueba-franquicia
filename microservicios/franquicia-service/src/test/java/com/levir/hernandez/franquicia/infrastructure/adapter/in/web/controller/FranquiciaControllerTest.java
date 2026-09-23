package com.levir.hernandez.franquicia.infrastructure.adapter.in.web.controller;

import com.levir.hernandez.franquicia.application.exception.FranquiciaNoEncontradaException;
import com.levir.hernandez.franquicia.application.port.in.franquicia.AgregarFranquiciaUseCase;
import com.levir.hernandez.franquicia.application.port.in.franquicia.ObtenerFranquiciaUseCase;
import com.levir.hernandez.franquicia.application.port.in.franquicia.ObtenerFranquiciasUseCase;
import com.levir.hernandez.franquicia.application.port.in.franquicia.RenombrarFranquiciaUseCase;
import com.levir.hernandez.franquicia.domain.model.Franquicia;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.assembler.FranquiciaAssembler;
import com.levir.hernandez.franquicia.infrastructure.config.HateoasConfig;
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

@WebFluxTest(FranquiciaController.class)
@Import({FranquiciaAssembler.class, HateoasConfig.class})
class FranquiciaControllerTest
{
    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AgregarFranquiciaUseCase agregarFranquicia;
    @MockitoBean
    private ObtenerFranquiciaUseCase obtenerFranquicia;
    @MockitoBean
    private ObtenerFranquiciasUseCase obtenerFranquicias;
    @MockitoBean
    private RenombrarFranquiciaUseCase renombrarFranquicia;

    private final UUID franquiciaId = UUID.randomUUID();

    private WebTestClient.ResponseSpec enviar(WebTestClient.RequestBodyUriSpec metodo, String uri, Object[] ids,
                                              String json)
    {
        return metodo.uri(uri, ids).contentType(MediaType.APPLICATION_JSON).bodyValue(json).exchange();
    }

    @Test
    @DisplayName("Deberia responder 201 al agregar una franquicia")
    void agregaFranquicia()
    {
        when(agregarFranquicia.agregarFranquicia("Burger Express"))
                .thenReturn(Mono.just(new Franquicia(franquiciaId, "Burger Express")));

        enviar(webTestClient.post(), "/api/v1/franquicias", new Object[0], "{\"nombre\": \"Burger Express\"}")
                .expectStatus().isCreated()
                .expectHeader().exists("Location")
                .expectBody().jsonPath("$.nombre").isEqualTo("Burger Express");
    }

    @Test
    @DisplayName("Deberia responder 400 al agregar una franquicia sin nombre")
    void noAgregaFranquiciaSinNombre()
    {
        enviar(webTestClient.post(), "/api/v1/franquicias", new Object[0], "{\"nombre\": \"\"}")
                .expectStatus().isBadRequest();

        verifyNoInteractions(agregarFranquicia);
    }

    @Test
    @DisplayName("Deberia responder 400 al agregar una franquicia con un cuerpo mal formado")
    void noAgregaFranquiciaConCuerpoMalFormado()
    {
        enviar(webTestClient.post(), "/api/v1/franquicias", new Object[0], "{nombre")
                .expectStatus().isBadRequest();

        verifyNoInteractions(agregarFranquicia);
    }

    @Test
    @DisplayName("Deberia responder 200 al listar las franquicias")
    void listaFranquicias()
    {
        when(obtenerFranquicias.obtenerFranquicias())
                .thenReturn(Flux.just(new Franquicia(franquiciaId, "Burger Express")));

        webTestClient.get().uri("/api/v1/franquicias").exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$._embedded.franquicias[0].nombre").isEqualTo("Burger Express");
    }

    @Test
    @DisplayName("Deberia responder 200 al obtener una franquicia existente")
    void obtieneFranquicia()
    {
        when(obtenerFranquicia.obtenerFranquicia(franquiciaId))
                .thenReturn(Mono.just(new Franquicia(franquiciaId, "Burger Express")));

        webTestClient.get().uri("/api/v1/franquicias/{id}", franquiciaId).exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.nombre").isEqualTo("Burger Express")
                .jsonPath("$._links.self.href").exists();
    }

    @Test
    @DisplayName("Deberia responder 400 al obtener una franquicia con un id que no es UUID")
    void noObtieneFranquiciaConIdInvalido()
    {
        webTestClient.get().uri("/api/v1/franquicias/{id}", "no-es-uuid").exchange()
                .expectStatus().isBadRequest();

        verifyNoInteractions(obtenerFranquicia);
    }

    @Test
    @DisplayName("Deberia responder 404 al obtener una franquicia que no existe")
    void noObtieneFranquiciaInexistente()
    {
        when(obtenerFranquicia.obtenerFranquicia(franquiciaId))
                .thenReturn(Mono.error(new FranquiciaNoEncontradaException(franquiciaId)));

        webTestClient.get().uri("/api/v1/franquicias/{id}", franquiciaId).exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Deberia responder 200 al renombrar una franquicia")
    void renombraFranquicia()
    {
        when(renombrarFranquicia.renombrarFranquicia(franquiciaId, "Pizza Rapida"))
                .thenReturn(Mono.just(new Franquicia(franquiciaId, "Pizza Rapida")));

        enviar(webTestClient.patch(), "/api/v1/franquicias/{id}/nombre", new Object[]{franquiciaId},
                "{\"nombre\": \"Pizza Rapida\"}")
                .expectStatus().isOk()
                .expectBody().jsonPath("$.nombre").isEqualTo("Pizza Rapida");
    }

    @Test
    @DisplayName("Deberia responder 400 al renombrar una franquicia sin nombre")
    void noRenombraFranquiciaSinNombre()
    {
        enviar(webTestClient.patch(), "/api/v1/franquicias/{id}/nombre", new Object[]{franquiciaId},
                "{\"nombre\": \"\"}")
                .expectStatus().isBadRequest();

        verifyNoInteractions(renombrarFranquicia);
    }

    @Test
    @DisplayName("Deberia responder 404 al renombrar una franquicia que no existe")
    void noRenombraFranquiciaInexistente()
    {
        when(renombrarFranquicia.renombrarFranquicia(franquiciaId, "Pizza Rapida"))
                .thenReturn(Mono.error(new FranquiciaNoEncontradaException(franquiciaId)));

        enviar(webTestClient.patch(), "/api/v1/franquicias/{id}/nombre", new Object[]{franquiciaId},
                "{\"nombre\": \"Pizza Rapida\"}")
                .expectStatus().isNotFound();
    }
}
