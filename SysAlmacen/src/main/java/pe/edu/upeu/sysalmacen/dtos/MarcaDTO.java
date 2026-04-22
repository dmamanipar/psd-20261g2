package pe.edu.upeu.sysalmacen.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MarcaDTO {
    private Long idMarca;

    @NotNull
    @Size(min = 4, max = 60, message = "{name.size}")
    private String nombre;
}
