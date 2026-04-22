package pe.edu.upeu.sysalmacen.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class VentaDTO {
    private Long idVenta;
    private Double precioBase;
    private Double igv;
    private Double precioTotal;
    private ClienteDTO cliente;
    private UsuarioDTO usuario;
    private String numDoc;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaGener;
    private String serie;
    private String tipoDoc;

    private List<VentaDetalleDTO> ventaDetalles;

    public record VentaCADTO(
             Long idVenta,
             Double precioBase,
             Double igv,
             Double precioTotal,
             String cliente,
             Long usuario,
             String numDoc,
             @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
             LocalDateTime fechaGener,
             String serie,
             String tipoDoc
    ){}
}
