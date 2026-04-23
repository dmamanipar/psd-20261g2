package pe.edu.upeu.sysalmacen.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;
import pe.edu.upeu.sysalmacen.config.MySQLTestContainer;
import pe.edu.upeu.sysalmacen.dtos.UsuarioDTO;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Clase base para TODAS las pruebas de integración de Controllers.
 *
 * Centraliza:
 *  1. Configuración Testcontainers + MySQL real (Replace.NONE).
 *  2. Inyección dinámica del datasource via @DynamicPropertySource.
 *  3. Obtención del token JWT (registro o login automático).
 *
 * Todas las subclases heredan esta configuración sin repetirla.
 * El contenedor MySQL arranca UNA sola vez gracias al Singleton
 * MySQLTestContainer.INSTANCE.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("tc")
public abstract class BaseIntegrationTest {

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      MySQLTestContainer.INSTANCE::getJdbcUrl);
        registry.add("spring.datasource.username", MySQLTestContainer.INSTANCE::getUsername);
        registry.add("spring.datasource.password", MySQLTestContainer.INSTANCE::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected static final String USER_TEST     = "testuser@upeu.edu.pe";
    protected static final String PASSWORD_TEST = "Test1234*";
    protected static final String ROL_TEST      = "ADMIN";

    /**
     * Registra un usuario de prueba y retorna el token JWT.
     * Si el usuario ya existe (HTTP 400), hace login directamente.
     */
    protected String obtenerTokenJwt() throws Exception {
        UsuarioDTO.UsuarioCrearDto registro = new UsuarioDTO.UsuarioCrearDto(
                USER_TEST, PASSWORD_TEST.toCharArray(), ROL_TEST, "Activo");

        MvcResult resultRegistro = mockMvc.perform(
                post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registro)))
                .andReturn();

        String body = resultRegistro.getResponse().getContentAsString();

        if (resultRegistro.getResponse().getStatus() != 201) {
            MvcResult resultLogin = mockMvc.perform(
                    post("/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new UsuarioDTO.CredencialesDto(
                                            USER_TEST, PASSWORD_TEST.toCharArray()))))
                    .andReturn();
            body = resultLogin.getResponse().getContentAsString();
        }

        return objectMapper.readValue(body, UsuarioDTO.class).getToken();
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }
}
