package com.levir.hernandez.franquicia.infrastructure.adapter.in.web.controller;

import com.levir.hernandez.franquicia.application.exception.FranquiciaNoEncontradaException;
import com.levir.hernandez.franquicia.application.port.in.franquicia.AgregarFranquiciaUseCase;
import com.levir.hernandez.franquicia.application.port.in.franquicia.ObtenerFranquiciaUseCase;
import com.levir.hernandez.franquicia.application.port.in.franquicia.ObtenerFranquiciasUseCase;
import com.levir.hernandez.franquicia.application.port.in.franquicia.RenombrarFranquiciaUseCase;
import com.levir.hernandez.franquicia.domain.model.Franquicia;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.assembler.FranquiciaAssembler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FranquiciaController.class)
@Import(FranquiciaAssembler.class)
class FranquiciaControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgregarFranquiciaUseCase agregarFranquicia;
    @MockitoBean
    private ObtenerFranquiciaUseCase obtenerFranquicia;
    @MockitoBean
    private ObtenerFranquiciasUseCase obtenerFranquicias;
    @MockitoBean
    private RenombrarFranquiciaUseCase renombrarFranquicia;

    private final UUID franquiciaId = UUID.randomUUID();

    @Test
    @DisplayName("Deberia responder 201 al agregar una franquicia")
    void agregaFranquicia() throws Exception
    {
        when(agregarFranquicia.agregarFranquicia("Burger Express"))
                .thenReturn(new Franquicia(franquiciaId, "Burger Express"));

        mockMvc.perform(post("/api/v1/franquicias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"Burger Express\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.nombre").value("Burger Express"));
    }

    @Test
    @DisplayName("Deberia responder 400 al agregar una franquicia sin nombre")
    void noAgregaFranquiciaSinNombre() throws Exception
    {
        mockMvc.perform(post("/api/v1/franquicias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(agregarFranquicia);
    }

    @Test
    @DisplayName("Deberia responder 400 al agregar una franquicia con un cuerpo mal formado")
    void noAgregaFranquiciaConCuerpoMalFormado() throws Exception
    {
        mockMvc.perform(post("/api/v1/franquicias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{nombre"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(agregarFranquicia);
    }

    @Test
    @DisplayName("Deberia responder 200 al listar las franquicias")
    void listaFranquicias() throws Exception
    {
        when(obtenerFranquicias.obtenerFranquicias()).thenReturn(List.of(new Franquicia(franquiciaId, "Burger Express")));

        mockMvc.perform(get("/api/v1/franquicias"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deberia responder 200 al obtener una franquicia existente")
    void obtieneFranquicia() throws Exception
    {
        when(obtenerFranquicia.obtenerFranquicia(franquiciaId)).thenReturn(new Franquicia(franquiciaId, "Burger Express"));

        mockMvc.perform(get("/api/v1/franquicias/{id}", franquiciaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Burger Express"));
    }

    @Test
    @DisplayName("Deberia responder 400 al obtener una franquicia con un id que no es UUID")
    void noObtieneFranquiciaConIdInvalido() throws Exception
    {
        mockMvc.perform(get("/api/v1/franquicias/{id}", "no-es-uuid"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(obtenerFranquicia);
    }

    @Test
    @DisplayName("Deberia responder 404 al obtener una franquicia que no existe")
    void noObtieneFranquiciaInexistente() throws Exception
    {
        when(obtenerFranquicia.obtenerFranquicia(franquiciaId)).thenThrow(new FranquiciaNoEncontradaException(franquiciaId));

        mockMvc.perform(get("/api/v1/franquicias/{id}", franquiciaId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deberia responder 200 al renombrar una franquicia")
    void renombraFranquicia() throws Exception
    {
        when(renombrarFranquicia.renombrarFranquicia(franquiciaId, "Pizza Rapida"))
                .thenReturn(new Franquicia(franquiciaId, "Pizza Rapida"));

        mockMvc.perform(patch("/api/v1/franquicias/{id}/nombre", franquiciaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"Pizza Rapida\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Pizza Rapida"));
    }

    @Test
    @DisplayName("Deberia responder 400 al renombrar una franquicia sin nombre")
    void noRenombraFranquiciaSinNombre() throws Exception
    {
        mockMvc.perform(patch("/api/v1/franquicias/{id}/nombre", franquiciaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(renombrarFranquicia);
    }

    @Test
    @DisplayName("Deberia responder 404 al renombrar una franquicia que no existe")
    void noRenombraFranquiciaInexistente() throws Exception
    {
        when(renombrarFranquicia.renombrarFranquicia(franquiciaId, "Pizza Rapida"))
                .thenThrow(new FranquiciaNoEncontradaException(franquiciaId));

        mockMvc.perform(patch("/api/v1/franquicias/{id}/nombre", franquiciaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"Pizza Rapida\"}"))
                .andExpect(status().isNotFound());
    }
}
