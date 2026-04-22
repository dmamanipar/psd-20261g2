package pe.edu.upeu.sysalmacen.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class VentaDetalleDTO {
    private Long idVentaDetalle;
    private Double pu;
    private Double cantidad;
    private Double descuento;
    private Double subtotal;
    @JsonIgnoreProperties({"ventaDetalles"})
    private VentaDTO venta;
    private ProductoDTO producto;


    public record VentaDetalleCADTO(
            Long idVentaDetalle,
            Double pu,
            Double cantidad,
            Double descuento,
            Double subtotal,
            Long venta,
            Long producto
    ){}
}
