package pe.edu.upeu.sysalmacen.dtos;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class VentCarritoDTO {

    private Long idCarrito;
    private String dniruc;
    private ProductoDTO producto;
    private String nombreProducto;
    private Double cantidad;
    private Double punitario;
    private Double ptotal;
    private int estado;
    private UsuarioDTO usuario;

    public record VentCarritoCADTO(
            Long idCarrito,
            @NotNull(message = "El DNI/RUC no puede ser nulo")
            @Size(min = 8, max = 11, message = "El DNI/RUC debe tener entre 8 y 11 Dígitos")
            String dniruc,
            @NotNull(message = "El producto no puede ser nulo")
            Long producto,
            @NotNull(message = "El nombre del producto no puede ser nulo")
            String nombreProducto,
            @DecimalMin(value = "1.0", inclusive = true, message = "La cantidad debe ser mayor o igual a 1")
            Double cantidad,
            @NotNull(message = "El precio unitario no puede ser nulo")
            @DecimalMin(value = "0.0", inclusive = true, message = "El precio unitario debe ser mayor o igual a 0")
            Double punitario,
            @DecimalMin(value = "0.0", inclusive = true, message = "El precio total debe ser mayor o igual a 0")
            Double ptotal,
            int estado,
            @NotNull(message = "El usuario no puede ser nulo")
            Long usuario
    ){}

}
