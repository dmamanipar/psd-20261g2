package pe.edu.upeu.sysalmacen.control;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;

/**
 * Clase base para TODAS las pruebas de integración con RestAssured.
 *
 * Soporta DOS modos de ejecución:
 *   - Testcontainers : mvn test -Dspring.profiles.active=tc
 *   - Servidor externo: mvn test -Dexternal.base-url=http://localhost:8080
 *
 * Centraliza:
 *  1. Configuración de la URL base (propiedad del sistema o localhost:8080).
 *  2. Obtención del token JWT (registro o login automático).
 *  3. Helper givenAuth() para reutilizar autenticación en cada test.
 */
public abstract class BaseIntegrationTest {

    protected static final String USER_TEST     = "testuser@upeu.edu.pe";
    protected static final String PASSWORD_TEST = "Test1234*";
    protected static final String ROL_TEST      = "ADMIN";

    @BeforeAll
    static void configurarRestAssured() {
        String baseUrl = System.getProperty("external.base-url", "http://localhost:8080");
        RestAssured.baseURI = baseUrl;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    /**
     * Registra un usuario de prueba y retorna el token JWT.
     * Si el usuario ya existe (HTTP != 201), hace login directamente.
     */
    protected String obtenerTokenJwt() {
        String registroBody = String.format(
                "{\"user\":\"%s\",\"clave\":\"%s\",\"rol\":\"%s\",\"estado\":\"Activo\"}",
                USER_TEST, PASSWORD_TEST, ROL_TEST);
        String loginBody = String.format(
                "{\"user\":\"%s\",\"clave\":\"%s\"}", USER_TEST, PASSWORD_TEST);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(registroBody)
                .when()
                .post("/users/register")
                .statusCode();


        // Si el registro fue exitoso intenta extraer el token; sino, login
        String responseBody = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginBody)
                .when()
                .post("/users/login")
                .getBody().asString();

        return JsonPath.from(responseBody).getString("token");
    }

    /**
     * Prepara un RequestSpecification con Authorization Bearer y ContentType JSON.
     */
    protected RequestSpecification givenAuth(String token) {
        return RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }
}