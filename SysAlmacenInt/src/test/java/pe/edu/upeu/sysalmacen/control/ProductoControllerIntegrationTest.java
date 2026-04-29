package pe.edu.upeu.sysalmacen.control;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.*;

/**
 * Pruebas de integración para ProductoController.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Pruebas de integración - ProductoController")
class ProductoControllerIntegrationTest extends BaseIntegrationTest {

    private String token;
    private Long categoriaId;
    private Long marcaId;
    private Long unidadMedidaId;
    private Long productoId;

    @BeforeAll
    void setup() {
        token = obtenerTokenJwt();

        // Crear Categoría (retorna 201 con Location)
        categoriaId = crearEntidadYObtenerIdDesdeLocation(
                "/categorias", "{\"nombre\": \"Electrónica\"}");

        // Crear Marca (retorna 200, usar buscarmaxid)
        givenAuth(token)
                .body("{\"nombre\": \"Sony\"}")
                .when()
                .post("/marcas")
                .then()
                .statusCode(200);
        marcaId = Long.parseLong(
                givenAuth(token).when().get("/marcas/buscarmaxid")
                        .then().statusCode(200).extract().asString());

        // Crear UnidadMedida (retorna 201 con Location)
        unidadMedidaId = crearEntidadYObtenerIdDesdeLocation(
                "/unidadmedidas", "{\"nombreMedida\": \"Unidad\"}");
    }

    /** Crea una entidad por POST y extrae el id del header Location. */
    private Long crearEntidadYObtenerIdDesdeLocation(String endpoint, String body) {
        String location = givenAuth(token)
                .body(body)
                .when()
                .post(endpoint)
                .then()
                .statusCode(201)
                .extract()
                .header("Location");
        String[] partes = location.split("/");
        return Long.parseLong(partes[partes.length - 1]);
    }

    /**
     * Genera el JSON para ProductoCADto según el OpenAPI spec.
     * Campos: categoria, marca, unidadMedida (IDs directos)
     */
    private String productoBody(Long id, String nombre) {
        String idPart = id == null ? "" : "\"idProducto\":" + id + ",";
        return String.format(
                "{%s\"nombre\":\"%s\",\"pu\":150.0,\"puOld\":140.0," +
                        "\"utilidad\":10.0,\"stock\":50.0,\"stockOld\":45.0," +
                        "\"categoria\":%d,\"marca\":%d,\"unidadMedida\":%d}",
                idPart, nombre, categoriaId, marcaId, unidadMedidaId);
    }

    // ─── POST /productos ─────────────────────────────────────────────────────

    @Order(1)
    @DisplayName("POST /productos - crea producto y retorna HTTP 201 con Location")
    @Test
    void testCrearProducto_RetornaCreated() {
        String location = givenAuth(token)
                .body(productoBody(null, "Audífonos Bluetooth"))
                .when()
                .post("/productos")
                .then()
                .statusCode(201)
                .header("Location", notNullValue())
                .extract()
                .header("Location");

        String[] partes = location.split("/");
        productoId = Long.parseLong(partes[partes.length - 1]);
        Assertions.assertNotNull(productoId);
    }


    @Order(2)
    @DisplayName("POST /productos - sin token retorna 401 o 403")
    @Test
    void testCrearProducto_SinToken_RetornaUnauthorized() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(productoBody(null, "SinAuth"))
                .when()
                .post("/productos")
                .then()
                .statusCode(anyOf(is(401), is(403)));
    }

    // ─── GET /productos ──────────────────────────────────────────────────────

    @Order(3)
    @DisplayName("GET /productos - retorna lista con HTTP 200")
    @Test
    void testListarProductos_RetornaOk() {
        givenAuth(token)
                .when()
                .get("/productos")
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(1)));
    }

    // ─── GET /productos/{id} ─────────────────────────────────────────────────

    @Order(4)
    @DisplayName("GET /productos/{id} - retorna producto por id")
    @Test
    void testBuscarProductoPorId_RetornaOk() {
        Assertions.assertNotNull(productoId);

        givenAuth(token)
                .when()
                .get("/productos/{id}", productoId)
                .then()
                .statusCode(200)
                .body("idProducto", is(productoId.intValue()))
                .body("nombre", is("Audífonos Bluetooth"));
    }

    @Order(5)
    @DisplayName("GET /productos/{id} - id inexistente retorna HTTP 404")
    @Test
    void testBuscarProductoPorId_IdInexistente_RetornaNotFound() {
        givenAuth(token)
                .when()
                .get("/productos/{id}", 999999L)
                .then()
                .statusCode(404);
    }

    // ─── PUT /productos/{id} ─────────────────────────────────────────────────

    @Order(6)
    @DisplayName("PUT /productos/{id} - actualiza producto y retorna HTTP 200")
    @Test
    void testActualizarProducto_RetornaOk() {
        Assertions.assertNotNull(productoId);

        givenAuth(token)
                .body(productoBody(productoId, "Audífonos Sony Actualizados"))
                .when()
                .put("/productos/{id}", productoId)
                .then()
                .statusCode(200)
                .body("idProducto", is(productoId.intValue()))
                .body("nombre", is("Audífonos Sony Actualizados"));
    }

    // ─── DELETE /productos/{id} ──────────────────────────────────────────────

    @Order(7)
    @DisplayName("DELETE /productos/{id} - elimina producto y retorna HTTP 204")
    @Test
    void testEliminarProducto_RetornaNoContent() {
        // Crear producto específico para eliminar
        String location = givenAuth(token)
                .body(productoBody(null, "ProductoAEliminar"))
                .when()
                .post("/productos")
                .then()
                .statusCode(201)
                .extract()
                .header("Location");

        String[] partes = location.split("/");
        Long idEliminar = Long.parseLong(partes[partes.length - 1]);

        givenAuth(token)
                .when()
                .delete("/productos/{id}", idEliminar)
                .then()
                .statusCode(204);

        // Verificar que ya no existe
        givenAuth(token)
                .when()
                .get("/productos/{id}", idEliminar)
                .then()
                .statusCode(404);
    }

    @Order(8)
    @DisplayName("DELETE /productos/{id} - id inexistente retorna HTTP 404")
    @Test
    void testEliminarProducto_IdInexistente_RetornaNotFound() {
        givenAuth(token)
                .when()
                .delete("/productos/{id}", 999999L)
                .then()
                .statusCode(404);
    }
}
