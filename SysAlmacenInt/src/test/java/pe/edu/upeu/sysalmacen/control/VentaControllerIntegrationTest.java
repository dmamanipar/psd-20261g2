package pe.edu.upeu.sysalmacen.control;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.*;

/**
 * Pruebas de integración para VentaController.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Pruebas de integración - VentaController")
class VentaControllerIntegrationTest extends BaseIntegrationTest {

    private static final String DNIRUC_CLIENTE = "77777777";
    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String token;
    private Long usuarioId;
    private Long productoId;

    @BeforeAll
    void setup() {
        token = obtenerTokenJwt();

        // Obtener id del usuario desde el login (ya viene en UsuarioDTO)
        usuarioId = obtenerUsuarioIdDesdeLogin();

        // Crear cliente para la venta (retorna 201)
        crearClienteSiNoExiste();

        // Crear Categoría (retorna 201 con Location)
        Long categoriaId = crearEntidadYObtenerIdDesdeLocation(
                "/categorias", "{\"nombre\": \"General\"}");

        // Crear Marca (retorna 200, usar buscarmaxid)
        givenAuth(token)
                .body("{\"nombre\": \"Genérica\"}")
                .when()
                .post("/marcas")
                .then().statusCode(200);
        Long marcaId = Long.parseLong(
                givenAuth(token).when().get("/marcas/buscarmaxid")
                        .then().statusCode(200).extract().asString());

        // Crear UnidadMedida (retorna 201 con Location)
        Long unidadId = crearEntidadYObtenerIdDesdeLocation(
                "/unidadmedidas", "{\"nombreMedida\": \"Unidad\"}");

        // Crear producto (retorna 201 con Location)
        String productoBody = String.format(
                "{\"nombre\":\"Producto de Prueba\",\"pu\":10.0,\"puOld\":10.0," +
                        "\"utilidad\":2.0,\"stock\":100.0,\"stockOld\":100.0," +
                        "\"categoria\":%d,\"marca\":%d,\"unidadMedida\":%d}",
                categoriaId, marcaId, unidadId);

        productoId = crearEntidadYObtenerIdDesdeLocation("/productos", productoBody);

    // Agregar ítem al carrito via endpoint /ventcarritos (retorna 201)
    String carritoBody = String.format(
            "{\"dniruc\":\"%s\",\"producto\":%d,\"nombreProducto\":\"Producto de Prueba\"," +
            "\"cantidad\":2.0,\"punitario\":10.0,\"ptotal\":20.0,\"estado\":1,\"usuario\":%d}",
            DNIRUC_CLIENTE, productoId, usuarioId);

    givenAuth(token)
            .body(carritoBody)
            .when()
            .post("/ventcarritos")
            .then().statusCode(201);
}

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Obtiene el idUsuario haciendo login nuevamente.
     */
    private Long obtenerUsuarioIdDesdeLogin() {
        String loginBody = String.format(
                "{\"user\":\"%s\",\"clave\":\"%s\"}", USER_TEST, PASSWORD_TEST);

        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginBody)
                .when()
                .post("/users/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getLong("idUsuario");
    }

    private void crearClienteSiNoExiste() {
        int status = givenAuth(token)
                .when()
                .get("/clientes/{id}", DNIRUC_CLIENTE)
                .statusCode();
        if (status == 404) {
            givenAuth(token)
                    .body("{\"dniruc\":\"" + DNIRUC_CLIENTE + "\"," +
                            "\"nombres\":\"Cliente de Prueba\",\"tipoDocumento\":\"DNI\"}")
                    .when()
                    .post("/clientes")
                    .then().statusCode(201);
        }
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

    // ─── POST /ventas ─────────────────────────────────────────────────────────

    @Order(1)
    @DisplayName("POST /ventas - procesa una venta desde el carrito y retorna HTTP 201")
    @Test
    void testProcesarVenta_RetornaCreated() {
        // Formato según @JsonFormat del DTO: "yyyy-MM-dd HH:mm:ss"
        String fechaHora = LocalDateTime.now().format(DT_FMT);
        
        String ventaBody = String.format(
                "{\"precioBase\":20.0,\"igv\":3.6,\"precioTotal\":23.6," +
                "\"cliente\":\"%s\",\"usuario\":%d," +
                "\"numDoc\":\"000001\",\"fechaGener\":\"%s\"," +
                "\"serie\":\"V001\",\"tipoDoc\":\"BOLETA\"}",
                DNIRUC_CLIENTE, usuarioId, fechaHora);

        givenAuth(token)
                .body(ventaBody)
                .when()
                .post("/ventas")
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .header("Location", notNullValue());
    }

    @Order(2)
    @DisplayName("POST /ventas - sin token retorna 401 o 403")
    @Test
    void testProcesarVenta_SinToken_RetornaUnauthorized() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"precioBase\":10.0,\"precioTotal\":10.0,\"cliente\":\"00000000\"}")
                .when()
                .post("/ventas")
                .then()
                .statusCode(anyOf(is(401), is(403)));
    }

    // ─── GET /ventas ──────────────────────────────────────────────────────────

    @Order(3)
    @DisplayName("GET /ventas - retorna lista de ventas con HTTP 200")
    @Test
    void testListarVentas_RetornaOk() {
        givenAuth(token)
                .when()
                .get("/ventas")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", isA(java.util.List.class));
    }

    @Order(4)
    @DisplayName("GET /ventcarritos/list/{dniruc} - verifica carrito del cliente")
    @Test
    void testListarCarritoCliente() {
        givenAuth(token)
                .when()
                .get("/ventcarritos/list/{dniruc}", DNIRUC_CLIENTE)
                .then()
                .statusCode(200)
                .body("$", isA(java.util.List.class));
    }
}
