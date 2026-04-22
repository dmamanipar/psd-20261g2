package pe.edu.upeu.sysalmacen.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UsuarioDTO {
    private Long idUsuario;
    @NotNull
    private String user;
    //@NotNull
    //private String clave;
    @NotNull
    private String estado;
    private String token;

    public record CredencialesDto(String user, char[] clave) { }

    public record UsuarioCrearDto(String user, char[] clave, String rol, String estado) { }
}