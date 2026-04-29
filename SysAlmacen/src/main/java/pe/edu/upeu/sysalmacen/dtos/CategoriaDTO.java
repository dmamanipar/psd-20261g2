package pe.edu.upeu.sysalmacen.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CategoriaDTO {
    private Long idCategoria;

    @Size(min = 3, max = 60)
    @NotEmpty
    @NotNull
    private String nombre;
}
