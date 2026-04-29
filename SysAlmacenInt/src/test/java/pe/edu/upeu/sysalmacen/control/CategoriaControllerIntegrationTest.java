package pe.edu.upeu.sysalmacen.control;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.Matchers.*;

/**
 * Pruebas de integración para CategoriaController.
 *
 * Funciona en DOS modos:
 *   - Testcontainers: mvn test -Dspring.profiles.active=tc
 *   - Servidor externo: mvn test -Dexternal.base-url=http://localhost:8080
 *
 * Usa RestAssured para hacer peticiones HTTP reales en ambos modos.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Pruebas de integración - CategoriaController")
class CategoriaControllerIntegrationTest extends BaseIntegrationTest {

    private String token;
    private Long categoriaId;

    @BeforeAll
    void autenticar() {
        token = obtenerTokenJwt();
    }

    // ─── POST /categorias ────────────────────────────────────────────────────

    @Order(1)
    @DisplayName("POST /categorias - crea categoria y retorna HTTP 201 con Location")
    @Test
    void testCrearCategoria_RetornaCreatedConLocation() {
        String location = givenAuth(token)
                .body("{\"nombre\": \"Electrónica\"}")
                .when()
                .post("/categorias")
                .then()
                .statusCode(201)
                .header("Location", notNullValue())
                .extract()
                .header("Location");

        String[] partes = location.split("/");
        categoriaId = Long.parseLong(partes[partes.length - 1]);
        Assertions.assertNotNull(categoriaId);
    }

    @Order(2)
    @DisplayName("POST /categorias - verifica diferentes nombres de categoria")
    @ParameterizedTest(name = "nombre=''{0}''")
    @ValueSource(strings = {"Ropa", "Calzado", "Alimentos"})
    void testCrearCategoria_ConDiferentesNombres(String nombre) {
        givenAuth(token)
                .body("{\"nombre\": \"" + nombre + "\"}")
                .when()
                .post("/categorias")
                .then()
                .statusCode(201)
                .header("Location", notNullValue());
    }

    @Order(3)
    @DisplayName("POST /categorias - sin token retorna 401 o 403")
    @Test
    void testCrearCategoria_SinToken_RetornaUnauthorized() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"nombre\": \"SinAuth\"}")
                .when()
                .post("/categorias")
                .then()
                .statusCode(anyOf(is(401), is(403)));
    }

    // ─── GET /categorias ─────────────────────────────────────────────────────

    @Order(4)
    @DisplayName("GET /categorias - retorna lista con HTTP 200")
    @Test
    void testListarCategorias_RetornaListaConHttpOk() {
        givenAuth(token)
                .when()
                .get("/categorias")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", isA(java.util.List.class));
    }

    // ─── GET /categorias/{id} ─────────────────────────────────────────────────

    @Order(5)
    @DisplayName("GET /categorias/{id} - retorna la categoria solicitada")
    @Test
    void testBuscarCategoriaPorId_RetornaCategoriaDTO() {
        Assertions.assertNotNull(categoriaId, "categoriaId debe existir");

        givenAuth(token)
                .when()
                .get("/categorias/{id}", categoriaId)
                .then()
                .statusCode(200)
                .body("idCategoria", is(categoriaId.intValue()))
                .body("nombre", is("Electrónica"));
    }

    @Order(6)
    @DisplayName("GET /categorias/{id} - id inexistente retorna 404")
    @Test
    void testBuscarCategoriaPorId_IdInexistente_RetornaNotFound() {
        givenAuth(token)
                .when()
                .get("/categorias/{id}", 999999L)
                .then()
                .statusCode(404);
    }

    // ─── PUT /categorias/{id} ─────────────────────────────────────────────────

    @Order(7)
    @DisplayName("PUT /categorias/{id} - actualiza y retorna HTTP 200")
    @Test
    void testActualizarCategoria_RetornaCategoriaActualizada() {
        Assertions.assertNotNull(categoriaId);

        givenAuth(token)
                .body("{\"idCategoria\": " + categoriaId + ", \"nombre\": \"Tecnología Actualizada\"}")
                .when()
                .put("/categorias/{id}", categoriaId)
                .then()
                .statusCode(200)
                .body("idCategoria", is(categoriaId.intValue()))
                .body("nombre", is("Tecnología Actualizada"));
    }

    // ─── DELETE /categorias/{id} ──────────────────────────────────────────────

    @Order(8)
    @DisplayName("DELETE /categorias/{id} - elimina y retorna HTTP 204")
    @Test
    void testEliminarCategoria_RetornaNoContent() {
        String location = givenAuth(token)
                .body("{\"nombre\": \"CategoriaAEliminar\"}")
                .when()
                .post("/categorias")
                .then()
                .statusCode(201)
                .extract()
                .header("Location");

        String[] partes = location.split("/");
        Long idEliminar = Long.parseLong(partes[partes.length - 1]);

        givenAuth(token)
                .when()
                .delete("/categorias/{id}", idEliminar)
                .then()
                .statusCode(204);
    }

    @Order(9)
    @DisplayName("DELETE /categorias/{id} - id inexistente retorna 404")
    @Test
    void testEliminarCategoria_IdInexistente_RetornaNotFound() {
        givenAuth(token)
                .when()
                .delete("/categorias/{id}", 999999L)
                .then()
                .statusCode(404);
    }
}
