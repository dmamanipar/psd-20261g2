package pe.edu.upeu.sysalmacen.config;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;
import java.util.List;
public final class MySQLTestContainer {
    public static final MySQLContainer<?> INSTANCE;
    static {
        INSTANCE = new
                MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                .withDatabaseName("sysalmacen_test")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true);
        INSTANCE.setPortBindings(List.of("3307:3306"));
        INSTANCE.start();
    }
    private MySQLTestContainer() {
    }
}
