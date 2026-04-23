package pe.edu.upeu.sysalmacen.repositorio;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import pe.edu.upeu.sysalmacen.config.MySQLTestContainer;
import pe.edu.upeu.sysalmacen.modelo.Categoria;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("tc")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Pruebas de repositorio - ICategoriaRepository (MySQL real)")
class ICategoriaRepositoryTest {

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                MySQLTestContainer.INSTANCE::getJdbcUrl);
        registry.add("spring.datasource.username",
                MySQLTestContainer.INSTANCE::getUsername);
        registry.add("spring.datasource.password",
                MySQLTestContainer.INSTANCE::getPassword);
        registry.add("spring.datasource.driver-class-name", () ->
                "com.mysql.cj.jdbc.Driver");
    }

    @Autowired
    private ICategoriaRepository categoriaRepository;

    @Autowired
    private IProductoRepository productoRepository;

    @Autowired
    private IVentaDetalleRepository ventaDetalleRepository;

    private Categoria categoriaGuardada;

    @BeforeEach
    void setUp() {
        ventaDetalleRepository.deleteAll();
        productoRepository.deleteAll();
        categoriaRepository.deleteAll();
        categoriaGuardada = categoriaRepository.save(
                Categoria.builder().nombre("Electrónica").build());
    }

    @Order(1)
    @DisplayName("Guardar categoria - debe persistir y asignar id generado")
    @Test
    void testGuardarCategoria_PersisteCategoriaConIdGenerado() {
        Categoria nueva = Categoria.builder().nombre("Ropa").build();
        Categoria guardada = categoriaRepository.save(nueva);
        assertNotNull(guardada.getIdCategoria());
        assertEquals("Ropa", guardada.getNombre());
    }


    @Order(2)
    @DisplayName("Guardar categoria - verifica diferentes nombres")
    @ParameterizedTest(name = "nombre=''{0}''")
    @ValueSource(strings = {"Calzado", "Alimentos", "Tecnología", "Hogar"})
    void testGuardarCategoria_ConDiferentesNombres(String nombre) {
        Categoria cat =
                categoriaRepository.save(Categoria.builder().nombre(nombre).build());
        Assertions.assertThat(cat.getIdCategoria()).isNotNull().isPositive();
        Assertions.assertThat(cat.getNombre()).isEqualTo(nombre);
    }

    @Order(3)
    @DisplayName("Buscar categoria por id - retorna categoria existente")
    @Test
    void testFindById_RetornaCategoriaExistente() {
        Optional<Categoria> resultado =
                categoriaRepository.findById(categoriaGuardada.getIdCategoria());
        assertTrue(resultado.isPresent());
        assertEquals("Electrónica", resultado.get().getNombre());
    }
    @Order(4)
    @DisplayName("Buscar categoria por id - retorna vacío cuando no existe")
    @Test
    void testFindById_RetornaVacioCuandoNoExiste() {
        assertFalse(categoriaRepository.findById(9999L).isPresent());
    }

    @Order(5)
    @DisplayName("Listar categorias - retorna todas las categorias guardadas")
    @Test
    void testFindAll_RetornaTodasLasCategorias() {
        categoriaRepository.save(Categoria.builder().nombre("Ropa").build());

        categoriaRepository.save(Categoria.builder().nombre("Calzado").build());
        List<Categoria> categorias = categoriaRepository.findAll();
        Assertions.assertThat(categorias).hasSize(3);
        Assertions.assertThat(categorias).extracting(Categoria::getNombre)
                .contains("Electrónica", "Ropa", "Calzado");
    }
    @Order(6)
    @DisplayName("Listar categorias - retorna lista vacía cuando no hay registros")
    @Test
    void testFindAll_RetornaListaVaciaSinRegistros() {
        categoriaRepository.deleteAll();
        Assertions.assertThat(categoriaRepository.findAll()).isEmpty();
    }

    @Order(7)
    @DisplayName("Actualizar categoria - debe persistir el nuevo nombre")
    @Test
    void testActualizarCategoria_PersisteCambios() {
        categoriaGuardada.setNombre("Tecnología Avanzada");
        Categoria actualizada = categoriaRepository.save(categoriaGuardada);
        assertEquals("Tecnología Avanzada", actualizada.getNombre());
        assertEquals(categoriaGuardada.getIdCategoria(),
                actualizada.getIdCategoria());
    }
    @Order(8)
    @DisplayName("Eliminar categoria - no debe existir después de eliminar")
    @Test
    void testEliminarCategoria_NoPuedeEncontrarsePosteriormente() {
        Long id = categoriaGuardada.getIdCategoria();
        categoriaRepository.deleteById(id);
        assertFalse(categoriaRepository.findById(id).isPresent());
    }
    @Order(9)
    @DisplayName("Eliminar categoria - el conteo debe decrementar")
    @Test
    void testEliminarCategoria_ReduceConteoTotal() {

        categoriaRepository.save(Categoria.builder().nombre("Extra").build());
        long totalAntes = categoriaRepository.count();
        categoriaRepository.deleteById(categoriaGuardada.getIdCategoria());

        Assertions.assertThat(categoriaRepository.count()).isEqualTo(totalAntes -1);
    }



}
