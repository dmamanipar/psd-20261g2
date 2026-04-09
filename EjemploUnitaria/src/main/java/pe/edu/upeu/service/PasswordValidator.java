package pe.edu.upeu.service;

public interface PasswordValidator {
    boolean isValid(String passw);
    int nivelSeguridad(String passw);
}
