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
public class ProductoDTO {
    private Long idProducto;
    @NotNull(message = "El nombre no puede ser nulo")
    @Size(min = 2, max = 120, message = "El nombre debe tener entre 2 y 120 caracteres")
    private String nombre;
    @NotNull(message = "El precio unitario no puede ser nulo")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio unitario debe ser mayor o igual a 0")
    private Double pu;
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio unitario antiguo debe ser mayor o igual a 0")
    private Double puOld;
    @DecimalMin(value = "0.0", inclusive = true, message = "La utilidad debe ser mayor o igual a 0")
    private Double utilidad;
    @NotNull(message = "El stock no puede ser nulo")
    @DecimalMin(value = "0.0", inclusive = true, message = "El stock debe ser mayor o igual a 0")
    private Double stock;
    @DecimalMin(value = "0.0", inclusive = true, message = "El stock antiguo debe ser mayor o igual a 0")
    private Double stockOld;
    @NotNull(message = "La categoría no puede ser nula")
    private CategoriaDTO categoria;
    @NotNull(message = "La marca no puede ser nula")
    private MarcaDTO marca;
    @NotNull(message = "La unidad de medida no puede ser nula")
    private UnidadMedidaDTO unidadMedida;

    public record ProductoCADto(
            Long idProducto,
            @NotNull(message = "El nombre no puede ser nulo")
            @Size(min = 2, max = 120, message = "El nombre debe tener entre 2 y 120 caracteres")
            String nombre,
            @NotNull(message = "El precio unitario no puede ser nulo")
            @DecimalMin(value = "0.0", inclusive = true, message = "El precio unitario debe ser mayor o igual a 0")
            Double pu,
            @DecimalMin(value = "0.0", inclusive = true, message = "El precio unitario antiguo debe ser mayor o igual a 0")
            Double puOld,
            @DecimalMin(value = "0.0", inclusive = true, message = "La utilidad debe ser mayor o igual a 0")
            Double utilidad,
            @NotNull(message = "El stock no puede ser nulo")
            @DecimalMin(value = "0.0", inclusive = true, message = "El stock debe ser mayor o igual a 0")
            Double stock,
            @DecimalMin(value = "0.0", inclusive = true, message = "El stock antiguo debe ser mayor o igual a 0")
            Double stockOld,
            @NotNull(message = "La categoría no puede ser nula")
            Long categoria,
            @NotNull(message = "La marca no puede ser nula")
            Long marca,
            @NotNull(message = "La unidad de medida no puede ser nula")
            Long unidadMedida   ){
    }



}
