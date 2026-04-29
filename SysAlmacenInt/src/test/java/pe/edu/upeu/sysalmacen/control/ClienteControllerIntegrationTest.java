package pe.edu.upeu.sysalmacen.control;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.Matchers.*;

/**
 * Pruebas de integración para ClienteController.
 *
 * Funciona en DOS modos:
 *   - Testcontainers: mvn test -Dspring.profiles.active=tc
 *   - Servidor externo: mvn test -Dexternal.base-url=http://localhost:8080
 *
 * El Cliente usa dniruc (String) como PK, no secuencia autogenerada.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Pruebas de integración - ClienteController")
class ClienteControllerIntegrationTest extends BaseIntegrationTest {

    private String token;
    private static final String DNIRUC_TEST = "12345678";

    @BeforeAll
    void autenticar() {
        token = obtenerTokenJwt();
    }

    // ─── POST /clientes ──────────────────────────────────────────────────────

    @Order(1)
    @DisplayName("POST /clientes - crea cliente y retorna HTTP 201 con Location")
    @Test
    void testCrearCliente_RetornaCreatedConLocation() {
        givenAuth(token)
                .body("{\"dniruc\":\"" + DNIRUC_TEST + "\",\"nombres\":\"Juan Pérez\"," +
                      "\"tipoDocumento\":\"DNI\",\"direccion\":\"Av. Lima 123\"}")
                .when()
                .post("/clientes")
                .then()
                .statusCode(201)
                .header("Location", containsString(DNIRUC_TEST));
    }

    @Order(2)
    @DisplayName("POST /clientes - crea clientes con diferentes tipos de documento")
    @ParameterizedTest(name = "dniruc=''{0}'', tipoDoc=''{2}''")
    @CsvSource({
        "20111222333, Empresa SAC, RUC",
        "87654321, María López, DNI"
    })
    void testCrearCliente_ConDiferentesDocumentos(String dniruc, String nombres, String tipoDoc) {
        givenAuth(token)
                .body("{\"dniruc\":\"" + dniruc + "\",\"nombres\":\"" + nombres + "\"," +
                      "\"tipoDocumento\":\"" + tipoDoc + "\"}")
                .when()
                .post("/clientes")
                .then()
                .statusCode(201)
                .header("Location", containsString(dniruc));
    }

    @Order(3)
    @DisplayName("POST /clientes - sin autenticacion retorna 401 o 403")
    @Test
    void testCrearCliente_SinToken_RetornaUnauthorized() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"dniruc\":\"99999999\",\"nombres\":\"Sin Auth\",\"tipoDocumento\":\"DNI\"}")
                .when()
                .post("/clientes")
                .then()
                .statusCode(anyOf(is(401), is(403)));
    }

    // ─── GET /clientes ───────────────────────────────────────────────────────

    @Order(4)
    @DisplayName("GET /clientes - retorna lista de clientes con HTTP 200")
    @Test
    void testListarClientes_RetornaListaConHttpOk() {
        givenAuth(token)
                .when()
                .get("/clientes")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", isA(java.util.List.class))
                .body("$", hasSize(greaterThanOrEqualTo(1)));
    }

    // ─── GET /clientes/{id} ──────────────────────────────────────────────────

    @Order(5)
    @DisplayName("GET /clientes/{id} - retorna cliente por dniruc")
    @Test
    void testBuscarClientePorDniruc_RetornaClienteDTO() {
        givenAuth(token)
                .when()
                .get("/clientes/{id}", DNIRUC_TEST)
                .then()
                .statusCode(200)
                .body("dniruc", is(DNIRUC_TEST))
                .body("nombres", is("Juan Pérez"));
    }

    @Order(6)
    @DisplayName("GET /clientes/{id} - dniruc inexistente retorna HTTP 404")
    @Test
    void testBuscarClientePorDniruc_Inexistente_RetornaNotFound() {
        givenAuth(token)
                .when()
                .get("/clientes/{id}", "00000000")
                .then()
                .statusCode(404);
    }

    // ─── PUT /clientes/{id} ──────────────────────────────────────────────────

    @Order(7)
    @DisplayName("PUT /clientes/{id} - actualiza cliente y retorna HTTP 200")
    @Test
    void testActualizarCliente_RetornaClienteActualizado() {
        givenAuth(token)
                .body("{\"dniruc\":\"" + DNIRUC_TEST + "\",\"nombres\":\"Juan Pérez Actualizado\"," +
                      "\"tipoDocumento\":\"DNI\",\"direccion\":\"Jr. Nuevo 999\"}")
                .when()
                .put("/clientes/{id}", DNIRUC_TEST)
                .then()
                .statusCode(200)
                .body("dniruc", is(DNIRUC_TEST))
                .body("nombres", is("Juan Pérez Actualizado"));
    }

    // ─── DELETE /clientes/{id} ───────────────────────────────────────────────

    @Order(8)
    @DisplayName("DELETE /clientes/{id} - elimina cliente y retorna HTTP 204")
    @Test
    void testEliminarCliente_RetornaNoContent() {
        // Crear cliente específico para eliminar
        givenAuth(token)
                .body("{\"dniruc\":\"11223344\",\"nombres\":\"AEliminar\",\"tipoDocumento\":\"DNI\"}")
                .when()
                .post("/clientes")
                .then()
                .statusCode(201);

        givenAuth(token)
                .when()
                .delete("/clientes/{id}", "11223344")
                .then()
                .statusCode(204);
    }

    @Order(9)
    @DisplayName("DELETE /clientes/{id} - dniruc inexistente retorna HTTP 404")
    @Test
    void testEliminarCliente_Inexistente_RetornaNotFound() {
        givenAuth(token)
                .when()
                .delete("/clientes/{id}", "00000000")
                .then()
                .statusCode(404);
    }
}
