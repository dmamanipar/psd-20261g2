package pe.edu.upeu.sysalmacen.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ClienteDTO {
    @NotNull(message = "El Ruc/DNI no puede ser nulo")
    @Size(min = 8, max = 11, message = "El RUC/DNI debe tener entre 8 y 11 digitos")
    private String dniruc;
    private String nombres;
    private String repLegal;
    private String tipoDocumento;
    private String direccion;
}
