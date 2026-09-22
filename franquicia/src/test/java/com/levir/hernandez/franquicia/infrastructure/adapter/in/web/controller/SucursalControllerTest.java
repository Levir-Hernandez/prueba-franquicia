package com.levir.hernandez.franquicia.infrastructure.adapter.in.web.controller;

import com.levir.hernandez.franquicia.application.exception.SucursalNoEncontradaException;
import com.levir.hernandez.franquicia.application.port.in.sucursal.AgregarSucursalUseCase;
import com.levir.hernandez.franquicia.application.port.in.sucursal.ObtenerSucursalUseCase;
import com.levir.hernandez.franquicia.application.port.in.sucursal.ObtenerSucursalesUseCase;
import com.levir.hernandez.franquicia.application.port.in.sucursal.RenombrarSucursalUseCase;
import com.levir.hernandez.franquicia.domain.model.Sucursal;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.assembler.SucursalAssembler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SucursalController.class)
@Import(SucursalAssembler.class)
class SucursalControllerTest
{
    @Autowired
    private MockMvc mockMvc;

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

    @Test
    @DisplayName("Deberia responder 201 al agregar una sucursal")
    void agregaSucursal() throws Exception
    {
        when(agregarSucursal.agregarSucursal(franquiciaId, "Burger Express Centro"))
                .thenReturn(sucursal("Burger Express Centro"));

        mockMvc.perform(post("/api/v1/franquicias/{id}/sucursales", franquiciaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"Burger Express Centro\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.nombre").value("Burger Express Centro"));
    }

    @Test
    @DisplayName("Deberia responder 400 al agregar una sucursal sin nombre")
    void noAgregaSucursalSinNombre() throws Exception
    {
        mockMvc.perform(post("/api/v1/franquicias/{id}/sucursales", franquiciaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(agregarSucursal);
    }

    @Test
    @DisplayName("Deberia responder 404 al agregar una sucursal a una franquicia que no existe")
    void noAgregaSucursalAFranquiciaInexistente() throws Exception
    {
        when(agregarSucursal.agregarSucursal(franquiciaId, "Burger Express Centro"))
                .thenThrow(new DataIntegrityViolationException("fk"));

        mockMvc.perform(post("/api/v1/franquicias/{id}/sucursales", franquiciaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"Burger Express Centro\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deberia responder 200 al listar las sucursales de una franquicia")
    void listaSucursales() throws Exception
    {
        when(obtenerSucursales.obtenerSucursales(franquiciaId)).thenReturn(List.of(sucursal("Burger Express Centro")));

        mockMvc.perform(get("/api/v1/franquicias/{id}/sucursales", franquiciaId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deberia responder 400 al listar sucursales con un id que no es UUID")
    void noListaSucursalesConIdInvalido() throws Exception
    {
        mockMvc.perform(get("/api/v1/franquicias/{id}/sucursales", "no-es-uuid"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(obtenerSucursales);
    }

    @Test
    @DisplayName("Deberia responder 200 al obtener una sucursal existente")
    void obtieneSucursal() throws Exception
    {
        when(obtenerSucursal.obtenerSucursal(sucursalId)).thenReturn(sucursal("Burger Express Centro"));

        mockMvc.perform(get("/api/v1/sucursales/{id}", sucursalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Burger Express Centro"));
    }

    @Test
    @DisplayName("Deberia responder 404 al obtener una sucursal que no existe")
    void noObtieneSucursalInexistente() throws Exception
    {
        when(obtenerSucursal.obtenerSucursal(sucursalId)).thenThrow(new SucursalNoEncontradaException(sucursalId));

        mockMvc.perform(get("/api/v1/sucursales/{id}", sucursalId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deberia responder 200 al renombrar una sucursal")
    void renombraSucursal() throws Exception
    {
        when(renombrarSucursal.renombrarSucursal(sucursalId, "Burger Express Poblado"))
                .thenReturn(sucursal("Burger Express Poblado"));

        mockMvc.perform(patch("/api/v1/sucursales/{id}/nombre", sucursalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"Burger Express Poblado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Burger Express Poblado"));
    }

    @Test
    @DisplayName("Deberia responder 400 al renombrar una sucursal sin nombre")
    void noRenombraSucursalSinNombre() throws Exception
    {
        mockMvc.perform(patch("/api/v1/sucursales/{id}/nombre", sucursalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(renombrarSucursal);
    }

    @Test
    @DisplayName("Deberia responder 404 al renombrar una sucursal que no existe")
    void noRenombraSucursalInexistente() throws Exception
    {
        when(renombrarSucursal.renombrarSucursal(sucursalId, "Burger Express Poblado"))
                .thenThrow(new SucursalNoEncontradaException(sucursalId));

        mockMvc.perform(patch("/api/v1/sucursales/{id}/nombre", sucursalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"Burger Express Poblado\"}"))
                .andExpect(status().isNotFound());
    }
}
