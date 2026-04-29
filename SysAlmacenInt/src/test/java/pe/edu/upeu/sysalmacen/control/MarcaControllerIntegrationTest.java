package pe.edu.upeu.sysalmacen.control;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.Matchers.*;

/**
 * Pruebas de integración para MarcaController.
 *
 * Funciona en DOS modos:
 *   - Testcontainers: mvn test -Dspring.profiles.active=tc
 *   - Servidor externo: mvn test -Dexternal.base-url=http://localhost:8080
 *
 * Nota: MarcaController retorna HTTP 200 (no 201) en creación,
 * y usa { "message": "true", "statusCode": 200 } como respuesta de éxito.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Pruebas de integración - MarcaController")
class MarcaControllerIntegrationTest extends BaseIntegrationTest {

    private String token;
    private Long marcaId;

    @BeforeAll
    void autenticar() {
        token = obtenerTokenJwt();
    }

    // ─── POST /marcas ────────────────────────────────────────────────────────

    @Order(1)
    @DisplayName("POST /marcas - crea marca y retorna HTTP 200 con message=true")
    @Test
    void testCrearMarca_RetornaOkConMessageTrue() {
        givenAuth(token)
                .body("{\"nombre\": \"MarcaIntegracion\"}")
                .when()
                .post("/marcas")
                .then()
                .statusCode(200)
                .body("message", is("true"));
    }

    @Order(2)
    @DisplayName("POST /marcas - verifica diferentes nombres de marca")
    @ParameterizedTest(name = "nombre=''{0}''")
    @ValueSource(strings = {"Nike", "Adidas", "Puma"})
    void testCrearMarca_ConDiferentesNombres(String nombre) {
        givenAuth(token)
                .body("{\"nombre\": \"" + nombre + "\"}")
                .when()
                .post("/marcas")
                .then()
                .statusCode(200)
                .body("message", is("true"))
                .body("statusCode", is(200));
    }

    @Order(3)
    @DisplayName("POST /marcas - sin token retorna 401 o 403")
    @Test
    void testCrearMarca_SinToken_RetornaForbiddenOUnauthorized() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"nombre\": \"MarcaSinAuth\"}")
                .when()
                .post("/marcas")
                .then()
                .statusCode(anyOf(is(401), is(403)));
    }

    // ─── GET /marcas ─────────────────────────────────────────────────────────

    @Order(4)
    @DisplayName("GET /marcas - retorna lista de marcas con HTTP 200")
    @Test
    void testListarMarcas_RetornaListaConHttpOk() {
        givenAuth(token)
                .when()
                .get("/marcas")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", isA(java.util.List.class));
    }

    // ─── GET /marcas/buscarmaxid ─────────────────────────────────────────────

    @Order(5)
    @DisplayName("GET /marcas/buscarmaxid - retorna Long con el max id existente")
    @Test
    void testObtenerMaxId_RetornaLongPositivo() {
        String respuesta = givenAuth(token)
                .when()
                .get("/marcas/buscarmaxid")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        marcaId = Long.parseLong(respuesta);
        Assertions.assertTrue(marcaId > 0, "El id máximo debe ser mayor a 0");
    }

    // ─── GET /marcas/{id} ────────────────────────────────────────────────────

    @Order(6)
    @DisplayName("GET /marcas/{id} - retorna la marca con el id solicitado")
    @Test
    void testBuscarMarcaPorId_RetornaMarcaDTO() {
        if (marcaId == null) {
            String resp = givenAuth(token)
                    .when()
                    .get("/marcas/buscarmaxid")
                    .then()
                    .statusCode(200)
                    .extract().asString();
            marcaId = Long.parseLong(resp);
        }

        givenAuth(token)
                .when()
                .get("/marcas/{id}", marcaId)
                .then()
                .statusCode(200)
                .body("idMarca", is(marcaId.intValue()));
    }

    @Order(7)
    @DisplayName("GET /marcas/{id} - retorna HTTP 404 cuando id no existe")
    @Test
    void testBuscarMarcaPorId_IdInexistente_RetornaNotFound() {
        givenAuth(token)
                .when()
                .get("/marcas/{id}", 999999L)
                .then()
                .statusCode(404);
    }

    // ─── PUT /marcas/{id} ────────────────────────────────────────────────────

    @Order(8)
    @DisplayName("PUT /marcas/{id} - actualiza la marca y retorna HTTP 200")
    @Test
    void testActualizarMarca_RetornaMarcaActualizada() {
        if (marcaId == null) {
            String resp = givenAuth(token)
                    .when()
                    .get("/marcas/buscarmaxid")
                    .then()
                    .statusCode(200)
                    .extract().asString();
            marcaId = Long.parseLong(resp);
        }

        givenAuth(token)
                .body("{\"idMarca\": " + marcaId + ", \"nombre\": \"MarcaActualizadaIT\"}")
                .when()
                .put("/marcas/{id}", marcaId)
                .then()
                .statusCode(200)
                .body("idMarca", is(marcaId.intValue()))
                .body("nombre", is("MarcaActualizadaIT"));
    }

    // ─── DELETE /marcas/{id} ─────────────────────────────────────────────────

    @Order(9)
    @DisplayName("DELETE /marcas/{id} - elimina la marca y retorna HTTP 200")
    @Test
    void testEliminarMarca_RetornaOkConMessageTrue() {
        // Obtener el id del último registro
        String idStr = givenAuth(token)
                .when()
                .get("/marcas/buscarmaxid")
                .then()
                .statusCode(200)
                .extract().asString();
        Long idEliminar = Long.parseLong(idStr);

        givenAuth(token)
                .when()
                .delete("/marcas/{id}", idEliminar)
                .then()
                .statusCode(200)
                .body("message", is("true"));
    }

    @Order(10)
    @DisplayName("DELETE /marcas/{id} - id inexistente retorna HTTP 404")
    @Test
    void testEliminarMarca_IdInexistente_RetornaNotFound() {
        givenAuth(token)
                .when()
                .delete("/marcas/{id}", 999999L)
                .then()
                .statusCode(404);
    }
}
