package pe.edu.upeu.sysalmacen.control;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.Matchers.*;

/**
 * Pruebas de integración para UnidadMedidaController.
 *
 * Funciona en DOS modos:
 *   - Testcontainers: mvn test -Dspring.profiles.active=tc
 *   - Servidor externo: mvn test -Dexternal.base-url=http://localhost:8080
 *
 * Usa RestAssured para hacer peticiones HTTP reales en ambos modos.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Pruebas de integración - UnidadMedidaController")
class UnidadMedidaControllerIntegrationTest extends BaseIntegrationTest {

    private String token;
    private Long unidadId;

    @BeforeAll
    void autenticar() {
        token = obtenerTokenJwt();
    }

    // ─── POST /unidadmedidas ─────────────────────────────────────────────────

    @Order(1)
    @DisplayName("POST /unidadmedidas - crea unidad de medida y retorna HTTP 201 con Location")
    @Test
    void testCrearUnidadMedida_RetornaCreatedConLocation() {
        String location = givenAuth(token)
                .body("{\"nombreMedida\": \"Kilogramo\"}")
                .when()
                .post("/unidadmedidas")
                .then()
                .statusCode(201)
                .header("Location", notNullValue())
                .extract()
                .header("Location");

        String[] partes = location.split("/");
        unidadId = Long.parseLong(partes[partes.length - 1]);
        Assertions.assertNotNull(unidadId);
    }

    @Order(2)
    @DisplayName("POST /unidadmedidas - verifica diferentes nombres de medida")
    @ParameterizedTest(name = "nombre=''{0}''")
    @ValueSource(strings = {"Litro", "Metro", "Gramo", "Unidad"})
    void testCrearUnidadMedida_ConDiferentesNombres(String nombre) {
        givenAuth(token)
                .body("{\"nombreMedida\": \"" + nombre + "\"}")
                .when()
                .post("/unidadmedidas")
                .then()
                .statusCode(201)
                .header("Location", notNullValue());
    }

    @Order(3)
    @DisplayName("POST /unidadmedidas - sin autenticacion retorna 401 o 403")
    @Test
    void testCrearUnidadMedida_SinToken_RetornaUnauthorized() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"nombreMedida\": \"SinAuth\"}")
                .when()
                .post("/unidadmedidas")
                .then()
                .statusCode(anyOf(is(401), is(403)));
    }

    // ─── GET /unidadmedidas ──────────────────────────────────────────────────

    @Order(4)
    @DisplayName("GET /unidadmedidas - retorna lista con HTTP 200")
    @Test
    void testListarUnidadesMedida_RetornaListaConHttpOk() {
        givenAuth(token)
                .when()
                .get("/unidadmedidas")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", isA(java.util.List.class))
                .body("$", hasSize(greaterThanOrEqualTo(1)));
    }

    // ─── GET /unidadmedidas/{id} ─────────────────────────────────────────────

    @Order(5)
    @DisplayName("GET /unidadmedidas/{id} - retorna unidad de medida por id")
    @Test
    void testBuscarUnidadPorId_RetornaUnidadMedidaDTO() {
        Assertions.assertNotNull(unidadId);

        givenAuth(token)
                .when()
                .get("/unidadmedidas/{id}", unidadId)
                .then()
                .statusCode(200)
                .body("idUnidad", is(unidadId.intValue()))
                .body("nombreMedida", is("Kilogramo"));
    }

    @Order(6)
    @DisplayName("GET /unidadmedidas/{id} - id inexistente retorna HTTP 404")
    @Test
    void testBuscarUnidadPorId_IdInexistente_RetornaNotFound() {
        givenAuth(token)
                .when()
                .get("/unidadmedidas/{id}", 999999L)
                .then()
                .statusCode(404);
    }

    // ─── PUT /unidadmedidas/{id} ─────────────────────────────────────────────

    @Order(7)
    @DisplayName("PUT /unidadmedidas/{id} - actualiza unidad y retorna HTTP 200")
    @Test
    void testActualizarUnidadMedida_RetornaUnidadActualizada() {
        Assertions.assertNotNull(unidadId);

        givenAuth(token)
                .body("{\"idUnidad\": " + unidadId + ", \"nombreMedida\": \"Gramo Actualizado\"}")
                .when()
                .put("/unidadmedidas/{id}", unidadId)
                .then()
                .statusCode(200)
                .body("idUnidad", is(unidadId.intValue()))
                .body("nombreMedida", is("Gramo Actualizado"));
    }

    // ─── DELETE /unidadmedidas/{id} ──────────────────────────────────────────

    @Order(8)
    @DisplayName("DELETE /unidadmedidas/{id} - elimina unidad y retorna HTTP 204")
    @Test
    void testEliminarUnidadMedida_RetornaNoContent() {
        // Crear unidad específica para eliminar
        String location = givenAuth(token)
                .body("{\"nombreMedida\": \"AEliminar\"}")
                .when()
                .post("/unidadmedidas")
                .then()
                .statusCode(201)
                .extract()
                .header("Location");

        String[] partes = location.split("/");
        Long idEliminar = Long.parseLong(partes[partes.length - 1]);

        givenAuth(token)
                .when()
                .delete("/unidadmedidas/{id}", idEliminar)
                .then()
                .statusCode(204);
    }

    @Order(9)
    @DisplayName("DELETE /unidadmedidas/{id} - id inexistente retorna HTTP 404")
    @Test
    void testEliminarUnidadMedida_IdInexistente_RetornaNotFound() {
        givenAuth(token)
                .when()
                .delete("/unidadmedidas/{id}", 999999L)
                .then()
                .statusCode(404);
    }
}
