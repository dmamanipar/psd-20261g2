package pe.edu.upeu.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@DisplayName("Operaciones Basicas")
public class ServicioATest {
    private ServicioA servicioA;
    @BeforeAll
    static void setup(){
        System.out.println("Iniciando Pruebas ServicioA");
    }
    @BeforeEach
    void setExec(){
        System.out.println("Ejecutado");
        servicioA=new ServicioAImp();
    }
    @Nested
    class OperacionesBasicas{
        @Test
        @DisplayName("Suma dos Valores")
        void sumaValores(){
            int resultado=servicioA.suma(5,6);
            Assertions.assertThat(resultado).isEqualTo(11);
        }
        @Test
        @DisplayName("Resta de dos Valores")
        void restaValores(){
            int resultado=servicioA.resta(8,6);
            Assertions.assertThat(resultado).isEqualTo(2);
        }
    }
    @Nested
    @DisplayName("Pruebas Parametrizadas")
    class Parametrizadas{

        @ParameterizedTest(name = "suma({0},{1})={2}")
        @CsvSource({"4,2,6", "7,2,9", "-10,2,-8"})
        void sumaParametrizada(int num1, int num2, int num3){
            Assertions.assertThat(servicioA.suma(num1, num2)).isEqualTo(num3);
        }

        @ParameterizedTest(name = "{0}/{1}={2}")
        @MethodSource("casosDePruebaDiv")
        void divicionParametrizada(int num1, int num2, double result){
            Assertions.assertThat(servicioA.dividir(num1, num2)).isEqualTo(result);
        }

        static Stream<Arguments> casosDePruebaDiv(){
            return Stream.of(
                    Arguments.of(10,2,5),
                    Arguments.of(-10,2,-5),
                    Arguments.of(9, 4, 2.25)

            );
        }

        @ParameterizedTest(name = "suma({0},{1})={2}")
        @CsvFileSource(resources = "/data/datatest.csv", numLinesToSkip = 1)
        @DisplayName("Pruebas con datos en archivo")
        void sumaParametrizadaFile(int num1, int num2, int num3){
            Assertions.assertThat(servicioA.suma(num1, num2)).isEqualTo(num3);
        }

    }

    @Test
    @DisplayName("Prueba Division entre cero")
    void divisionConCero(){
        Assertions.assertThatThrownBy(()->servicioA.dividir(6,0))
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("dividir entre cero");
    }

    @AfterAll
    static void finalizar(){
        System.out.println("Pruebas Finalizadas");
    }
}
