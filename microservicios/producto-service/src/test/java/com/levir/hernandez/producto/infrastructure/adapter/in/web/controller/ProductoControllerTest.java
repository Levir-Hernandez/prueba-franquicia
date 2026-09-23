package com.levir.hernandez.producto.infrastructure.adapter.in.web.controller;

import com.levir.hernandez.producto.application.exception.SucursalNoEncontradaException;
import com.levir.hernandez.producto.application.exception.ServicioNoDisponibleException;
import com.levir.hernandez.producto.application.exception.ProductoNoEncontradoException;
import com.levir.hernandez.producto.application.port.in.ObtenerProductosConMayorStockUseCase;
import com.levir.hernandez.producto.application.port.in.producto.*;
import com.levir.hernandez.producto.application.port.out.ProductoConMayorStock;
import com.levir.hernandez.producto.domain.model.Producto;
import com.levir.hernandez.producto.infrastructure.adapter.in.web.assembler.ProductoAssembler;
import com.levir.hernandez.producto.infrastructure.adapter.in.web.assembler.ProductoConMayorStockAssembler;
import com.levir.hernandez.producto.infrastructure.config.HateoasConfig;
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

import static org.mockito.Mockito.*;

@WebFluxTest(ProductoController.class)
@Import({ProductoAssembler.class, ProductoConMayorStockAssembler.class, HateoasConfig.class})
class ProductoControllerTest
{
    @Autowired
    private WebTestClient webTestClient;

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

    private WebTestClient.ResponseSpec enviar(WebTestClient.RequestBodyUriSpec metodo, String uri, Object id, String json)
    {
        return metodo.uri(uri, id).contentType(MediaType.APPLICATION_JSON).bodyValue(json).exchange();
    }

    @Test
    @DisplayName("Deberia responder 201 al agregar un producto")
    void agregaProducto()
    {
        when(agregarProducto.agregarProducto(sucursalId, "Hamburguesa clasica", 10))
                .thenReturn(Mono.just(producto("Hamburguesa clasica", 10)));

        enviar(webTestClient.post(), "/api/v1/sucursales/{id}/productos", sucursalId,
                "{\"nombre\": \"Hamburguesa clasica\", \"stock\": 10}")
                .expectStatus().isCreated()
                .expectHeader().exists("Location")
                .expectBody().jsonPath("$.stock").isEqualTo(10);
    }

    @Test
    @DisplayName("Deberia responder 400 al agregar un producto con stock negativo")
    void noAgregaProductoConStockNegativo()
    {
        enviar(webTestClient.post(), "/api/v1/sucursales/{id}/productos", sucursalId,
                "{\"nombre\": \"Hamburguesa clasica\", \"stock\": -1}")
                .expectStatus().isBadRequest();

        verifyNoInteractions(agregarProducto);
    }

    @Test
    @DisplayName("Deberia responder 404 al agregar un producto a una sucursal que no existe")
    void noAgregaProductoASucursalInexistente()
    {
        when(agregarProducto.agregarProducto(sucursalId, "Hamburguesa clasica", 10))
                .thenReturn(Mono.error(new SucursalNoEncontradaException(sucursalId)));

        enviar(webTestClient.post(), "/api/v1/sucursales/{id}/productos", sucursalId,
                "{\"nombre\": \"Hamburguesa clasica\", \"stock\": 10}")
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Deberia responder 503 al agregar un producto si el servicio de sucursales no esta disponible")
    void noAgregaProductoSiServicioNoDisponible()
    {
        when(agregarProducto.agregarProducto(sucursalId, "Hamburguesa clasica", 10))
                .thenReturn(Mono.error(new ServicioNoDisponibleException("sucursales")));

        enviar(webTestClient.post(), "/api/v1/sucursales/{id}/productos", sucursalId,
                "{\"nombre\": \"Hamburguesa clasica\", \"stock\": 10}")
                .expectStatus().isEqualTo(503);
    }

    @Test
    @DisplayName("Deberia responder 200 al listar los productos de una sucursal")
    void listaProductos()
    {
        when(obtenerProductos.obtenerProductos(sucursalId)).thenReturn(Flux.just(producto("Hamburguesa clasica", 10)));

        webTestClient.get().uri("/api/v1/sucursales/{id}/productos", sucursalId).exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$._embedded.productos[0].nombre").isEqualTo("Hamburguesa clasica");
    }

    @Test
    @DisplayName("Deberia responder 200 al obtener un producto existente")
    void obtieneProducto()
    {
        when(obtenerProducto.obtenerProducto(productoId)).thenReturn(Mono.just(producto("Hamburguesa clasica", 10)));

        webTestClient.get().uri("/api/v1/productos/{id}", productoId).exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.nombre").isEqualTo("Hamburguesa clasica")
                .jsonPath("$._links.self.href").exists();
    }

    @Test
    @DisplayName("Deberia responder 400 al obtener un producto con un id que no es UUID")
    void noObtieneProductoConIdInvalido()
    {
        webTestClient.get().uri("/api/v1/productos/{id}", "no-es-uuid").exchange()
                .expectStatus().isBadRequest();

        verifyNoInteractions(obtenerProducto);
    }

    @Test
    @DisplayName("Deberia responder 404 al obtener un producto que no existe")
    void noObtieneProductoInexistente()
    {
        when(obtenerProducto.obtenerProducto(productoId))
                .thenReturn(Mono.error(new ProductoNoEncontradoException(productoId)));

        webTestClient.get().uri("/api/v1/productos/{id}", productoId).exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Deberia responder 204 al eliminar un producto")
    void eliminaProducto()
    {
        when(eliminarProducto.eliminarProducto(productoId)).thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/v1/productos/{id}", productoId).exchange()
                .expectStatus().isNoContent();

        verify(eliminarProducto).eliminarProducto(productoId);
    }

    @Test
    @DisplayName("Deberia responder 404 al eliminar un producto que no existe")
    void noEliminaProductoInexistente()
    {
        when(eliminarProducto.eliminarProducto(productoId))
                .thenReturn(Mono.error(new ProductoNoEncontradoException(productoId)));

        webTestClient.delete().uri("/api/v1/productos/{id}", productoId).exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Deberia responder 200 al modificar el stock de un producto")
    void modificaStock()
    {
        when(modificarStockProducto.modificarStockProducto(productoId, 25))
                .thenReturn(Mono.just(producto("Hamburguesa clasica", 25)));

        enviar(webTestClient.patch(), "/api/v1/productos/{id}/stock", productoId, "{\"stock\": 25}")
                .expectStatus().isOk()
                .expectBody().jsonPath("$.stock").isEqualTo(25);
    }

    @Test
    @DisplayName("Deberia responder 400 al modificar el stock sin valor")
    void noModificaStockSinValor()
    {
        enviar(webTestClient.patch(), "/api/v1/productos/{id}/stock", productoId, "{}")
                .expectStatus().isBadRequest();

        verifyNoInteractions(modificarStockProducto);
    }

    @Test
    @DisplayName("Deberia responder 404 al modificar el stock de un producto que no existe")
    void noModificaStockProductoInexistente()
    {
        when(modificarStockProducto.modificarStockProducto(productoId, 25))
                .thenReturn(Mono.error(new ProductoNoEncontradoException(productoId)));

        enviar(webTestClient.patch(), "/api/v1/productos/{id}/stock", productoId, "{\"stock\": 25}")
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Deberia responder 200 al renombrar un producto")
    void renombraProducto()
    {
        when(renombrarProducto.renombrarProducto(productoId, "Hamburguesa doble"))
                .thenReturn(Mono.just(producto("Hamburguesa doble", 10)));

        enviar(webTestClient.patch(), "/api/v1/productos/{id}/nombre", productoId,
                "{\"nombre\": \"Hamburguesa doble\"}")
                .expectStatus().isOk()
                .expectBody().jsonPath("$.nombre").isEqualTo("Hamburguesa doble");
    }

    @Test
    @DisplayName("Deberia responder 400 al renombrar un producto sin nombre")
    void noRenombraProductoSinNombre()
    {
        enviar(webTestClient.patch(), "/api/v1/productos/{id}/nombre", productoId, "{\"nombre\": \"\"}")
                .expectStatus().isBadRequest();

        verifyNoInteractions(renombrarProducto);
    }

    @Test
    @DisplayName("Deberia responder 200 al listar los productos con mayor stock de una franquicia")
    void listaProductosConMayorStock()
    {
        when(obtenerProductosConMayorStock.obtenerProductosConMayorStock(franquiciaId)).thenReturn(Flux.just(
                new ProductoConMayorStock(productoId, "Hamburguesa clasica", 30, sucursalId, "Burger Express Centro")));

        webTestClient.get().uri("/api/v1/franquicias/{id}/productos/mayor-stock", franquiciaId).exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$._embedded.productos[0].sucursalNombre").isEqualTo("Burger Express Centro");
    }

    @Test
    @DisplayName("Deberia responder 400 al listar productos con mayor stock con un id que no es UUID")
    void noListaProductosConMayorStockConIdInvalido()
    {
        webTestClient.get().uri("/api/v1/franquicias/{id}/productos/mayor-stock", "no-es-uuid").exchange()
                .expectStatus().isBadRequest();

        verifyNoInteractions(obtenerProductosConMayorStock);
    }
}
