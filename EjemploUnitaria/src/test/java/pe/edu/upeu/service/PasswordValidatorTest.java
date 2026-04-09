package pe.edu.upeu.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Validar Password")
public class PasswordValidatorTest {

    private PasswordValidator validator;


    @BeforeEach
    void setup(){
        validator=new PasswordValidatorImp();
    }

    @Test
    void contrasenhaCompletaValida(){
        Assertions.assertThat(validator
                .isValid("Segura1!")).isTrue();
    }
    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Contraseña Nula")
    void contrasenhaNulo(String passw){
        Assertions.assertThat(validator.isValid(passw)).isFalse();
        Assertions.assertThat(validator.nivelSeguridad(passw)).isZero();
    }
    @ParameterizedTest
    @ValueSource(strings = {"abc", "123456", "PASSWORD", "password", "Password"})
    void contrasenhaConNivelIncompleta(String passw){
        Assertions.assertThat(validator.isValid(passw)).isFalse();
    }




}
