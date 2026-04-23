package pe.edu.upeu.sysalmacen.control;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import pe.edu.upeu.sysalmacen.dtos.CategoriaDTO;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración para CategoriaController.
 * Usa MySQL real via Testcontainers. Hereda configuración de BaseIntegrationTest.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Pruebas de integración - CategoriaController")
class CategoriaControllerIntegrationTest extends BaseIntegrationTest {

    private String token;
    private Long categoriaId;

    @BeforeAll
    void autenticar() throws Exception {
        token = obtenerTokenJwt();
    }

    // ─── POST /categorias ────────────────────────────────────────────────────

    @Order(1)
    @DisplayName("POST /categorias - crea categoria y retorna HTTP 201 con Location")
    @Test
    void testCrearCategoria_RetornaCreatedConLocation() throws Exception {
        CategoriaDTO dto = new CategoriaDTO(null, "Electrónica");

        String location = mockMvc.perform(post("/categorias")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn()
                .getResponse().getHeader("Location");

        // Extraer el id del header Location
        String[] partes = location.split("/");
        categoriaId = Long.parseLong(partes[partes.length - 1]);
        Assertions.assertNotNull(categoriaId);
    }

    @Order(2)
    @DisplayName("POST /categorias - verifica diferentes nombres de categoria")
    @ParameterizedTest(name = "nombre=''{0}''")
    @ValueSource(strings = {"Ropa", "Calzado", "Alimentos"})
    void testCrearCategoria_ConDiferentesNombres(String nombre) throws Exception {
        CategoriaDTO dto = new CategoriaDTO(null, nombre);

        mockMvc.perform(post("/categorias")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Order(3)
    @DisplayName("POST /categorias - sin autenticacion retorna 401 o 403")
    @Test
    void testCrearCategoria_SinToken_RetornaUnauthorized() throws Exception {
        CategoriaDTO dto = new CategoriaDTO(null, "SinAuth");

        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is(anyOf(is(401), is(403))));
    }

    // ─── GET /categorias ─────────────────────────────────────────────────────

    @Order(4)
    @DisplayName("GET /categorias - retorna lista de categorias con HTTP 200")
    @Test
    void testListarCategorias_RetornaListaConHttpOk() throws Exception {
        mockMvc.perform(get("/categorias")
                        .header("Authorization", bearer(token))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    // ─── GET /categorias/{id} ────────────────────────────────────────────────

    @Order(5)
    @DisplayName("GET /categorias/{id} - retorna la categoria con el id solicitado")
    @Test
    void testBuscarCategoriaPorId_RetornaCategoriaDTO() throws Exception {
        Assertions.assertNotNull(categoriaId, "categoriaId debe haber sido creado en Order 1");

        mockMvc.perform(get("/categorias/{id}", categoriaId)
                        .header("Authorization", bearer(token))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCategoria", is(categoriaId.intValue())))
                .andExpect(jsonPath("$.nombre", is("Electrónica")));
    }

    @Order(6)
    @DisplayName("GET /categorias/{id} - id inexistente retorna HTTP 404")
    @Test
    void testBuscarCategoriaPorId_IdInexistente_RetornaNotFound() throws Exception {
        mockMvc.perform(get("/categorias/{id}", 999999L)
                        .header("Authorization", bearer(token))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ─── PUT /categorias/{id} ────────────────────────────────────────────────

    @Order(7)
    @DisplayName("PUT /categorias/{id} - actualiza la categoria y retorna HTTP 200")
    @Test
    void testActualizarCategoria_RetornaCategoriaActualizada() throws Exception {
        Assertions.assertNotNull(categoriaId);
        CategoriaDTO dtoActualizado = new CategoriaDTO(categoriaId, "Tecnología Actualizada");

        mockMvc.perform(put("/categorias/{id}", categoriaId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoActualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCategoria", is(categoriaId.intValue())))
                .andExpect(jsonPath("$.nombre", is("Tecnología Actualizada")));
    }

    // ─── DELETE /categorias/{id} ─────────────────────────────────────────────

    @Order(8)
    @DisplayName("DELETE /categorias/{id} - elimina la categoria y retorna HTTP 204")
    @Test
    void testEliminarCategoria_RetornaNoContent() throws Exception {
        // Crear una categoria específica para eliminar
        CategoriaDTO dtoParaEliminar = new CategoriaDTO(null, "CategoriaAEliminar");
        String location = mockMvc.perform(post("/categorias")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoParaEliminar)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        String[] partes = location.split("/");
        Long idEliminar = Long.parseLong(partes[partes.length - 1]);

        mockMvc.perform(delete("/categorias/{id}", idEliminar)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
    }

    @Order(9)
    @DisplayName("DELETE /categorias/{id} - id inexistente retorna HTTP 404")
    @Test
    void testEliminarCategoria_IdInexistente_RetornaNotFound() throws Exception {
        mockMvc.perform(delete("/categorias/{id}", 999999L)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }
}
