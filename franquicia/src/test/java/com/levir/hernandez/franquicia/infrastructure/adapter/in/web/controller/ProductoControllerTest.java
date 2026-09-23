package com.levir.hernandez.franquicia.infrastructure.adapter.in.web.controller;

import com.levir.hernandez.franquicia.application.exception.ProductoNoEncontradoException;
import com.levir.hernandez.franquicia.application.port.in.ObtenerProductosConMayorStockUseCase;
import com.levir.hernandez.franquicia.application.port.in.producto.*;
import com.levir.hernandez.franquicia.application.port.out.ProductoConMayorStock;
import com.levir.hernandez.franquicia.domain.model.Producto;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.assembler.ProductoAssembler;
import com.levir.hernandez.franquicia.infrastructure.adapter.in.web.assembler.ProductoConMayorStockAssembler;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductoController.class)
@Import({ProductoAssembler.class, ProductoConMayorStockAssembler.class})
class ProductoControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgregarProductoUseCase agregarProducto;
    @MockitoBean
    private ObtenerProductoUseCase obtenerProducto;
    @MockitoBean
    private ObtenerProductosUseCase obtenerProductos;
    @MockitoBean
    private EliminarProductoUseCase eliminarProducto;
    @MockitoBean
    private ModificarStockProductoUseCase modificarStockProducto;
    @MockitoBean
    private RenombrarProductoUseCase renombrarProducto;
    @MockitoBean
    private ObtenerProductosConMayorStockUseCase obtenerProductosConMayorStock;

    private final UUID franquiciaId = UUID.randomUUID();
    private final UUID sucursalId = UUID.randomUUID();
    private final UUID productoId = UUID.randomUUID();

    private Producto producto(String nombre, int stock)
    {
        return new Producto(productoId, nombre, stock, sucursalId);
    }

    @Test
    @DisplayName("Deberia responder 201 al agregar un producto")
    void agregaProducto() throws Exception
    {
        when(agregarProducto.agregarProducto(sucursalId, "Hamburguesa clasica", 10))
                .thenReturn(producto("Hamburguesa clasica", 10));

        mockMvc.perform(post("/api/v1/sucursales/{id}/productos", sucursalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"Hamburguesa clasica\", \"stock\": 10}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.stock").value(10));
    }

    @Test
    @DisplayName("Deberia responder 400 al agregar un producto con stock negativo")
    void noAgregaProductoConStockNegativo() throws Exception
    {
        mockMvc.perform(post("/api/v1/sucursales/{id}/productos", sucursalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"Hamburguesa clasica\", \"stock\": -1}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(agregarProducto);
    }

    @Test
    @DisplayName("Deberia responder 404 al agregar un producto a una sucursal que no existe")
    void noAgregaProductoASucursalInexistente() throws Exception
    {
        when(agregarProducto.agregarProducto(sucursalId, "Hamburguesa clasica", 10))
                .thenThrow(new DataIntegrityViolationException("fk"));

        mockMvc.perform(post("/api/v1/sucursales/{id}/productos", sucursalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"Hamburguesa clasica\", \"stock\": 10}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deberia responder 200 al listar los productos de una sucursal")
    void listaProductos() throws Exception
    {
        when(obtenerProductos.obtenerProductos(sucursalId)).thenReturn(List.of(producto("Hamburguesa clasica", 10)));

        mockMvc.perform(get("/api/v1/sucursales/{id}/productos", sucursalId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deberia responder 200 al obtener un producto existente")
    void obtieneProducto() throws Exception
    {
        when(obtenerProducto.obtenerProducto(productoId)).thenReturn(producto("Hamburguesa clasica", 10));

        mockMvc.perform(get("/api/v1/productos/{id}", productoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Hamburguesa clasica"));
    }

    @Test
    @DisplayName("Deberia responder 400 al obtener un producto con un id que no es UUID")
    void noObtieneProductoConIdInvalido() throws Exception
    {
        mockMvc.perform(get("/api/v1/productos/{id}", "no-es-uuid"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(obtenerProducto);
    }

    @Test
    @DisplayName("Deberia responder 404 al obtener un producto que no existe")
    void noObtieneProductoInexistente() throws Exception
    {
        when(obtenerProducto.obtenerProducto(productoId)).thenThrow(new ProductoNoEncontradoException(productoId));

        mockMvc.perform(get("/api/v1/productos/{id}", productoId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deberia responder 204 al eliminar un producto")
    void eliminaProducto() throws Exception
    {
        mockMvc.perform(delete("/api/v1/productos/{id}", productoId))
                .andExpect(status().isNoContent());

        verify(eliminarProducto).eliminarProducto(productoId);
    }

    @Test
    @DisplayName("Deberia responder 404 al eliminar un producto que no existe")
    void noEliminaProductoInexistente() throws Exception
    {
        doThrow(new ProductoNoEncontradoException(productoId)).when(eliminarProducto).eliminarProducto(productoId);

        mockMvc.perform(delete("/api/v1/productos/{id}", productoId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deberia responder 200 al modificar el stock de un producto")
    void modificaStock() throws Exception
    {
        when(modificarStockProducto.modificarStockProducto(productoId, 25))
                .thenReturn(producto("Hamburguesa clasica", 25));

        mockMvc.perform(patch("/api/v1/productos/{id}/stock", productoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\": 25}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(25));
    }

    @Test
    @DisplayName("Deberia responder 400 al modificar el stock sin valor")
    void noModificaStockSinValor() throws Exception
    {
        mockMvc.perform(patch("/api/v1/productos/{id}/stock", productoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(modificarStockProducto);
    }

    @Test
    @DisplayName("Deberia responder 404 al modificar el stock de un producto que no existe")
    void noModificaStockProductoInexistente() throws Exception
    {
        when(modificarStockProducto.modificarStockProducto(productoId, 25))
                .thenThrow(new ProductoNoEncontradoException(productoId));

        mockMvc.perform(patch("/api/v1/productos/{id}/stock", productoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\": 25}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deberia responder 200 al renombrar un producto")
    void renombraProducto() throws Exception
    {
        when(renombrarProducto.renombrarProducto(productoId, "Hamburguesa doble"))
                .thenReturn(producto("Hamburguesa doble", 10));

        mockMvc.perform(patch("/api/v1/productos/{id}/nombre", productoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"Hamburguesa doble\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Hamburguesa doble"));
    }

    @Test
    @DisplayName("Deberia responder 400 al renombrar un producto sin nombre")
    void noRenombraProductoSinNombre() throws Exception
    {
        mockMvc.perform(patch("/api/v1/productos/{id}/nombre", productoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(renombrarProducto);
    }

    @Test
    @DisplayName("Deberia responder 200 al listar los productos con mayor stock de una franquicia")
    void listaProductosConMayorStock() throws Exception
    {
        when(obtenerProductosConMayorStock.obtenerProductosConMayorStock(franquiciaId)).thenReturn(List.of(
                new ProductoConMayorStock(productoId, "Hamburguesa clasica", 30, sucursalId, "Burger Express Centro")));

        mockMvc.perform(get("/api/v1/franquicias/{id}/productos/mayor-stock", franquiciaId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deberia responder 400 al listar productos con mayor stock con un id que no es UUID")
    void noListaProductosConMayorStockConIdInvalido() throws Exception
    {
        mockMvc.perform(get("/api/v1/franquicias/{id}/productos/mayor-stock", "no-es-uuid"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(obtenerProductosConMayorStock);
    }
}
