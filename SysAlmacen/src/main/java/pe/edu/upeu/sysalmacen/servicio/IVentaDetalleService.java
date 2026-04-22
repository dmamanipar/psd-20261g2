package pe.edu.upeu.sysalmacen.servicio;

import pe.edu.upeu.sysalmacen.dtos.VentaDetalleDTO;
import pe.edu.upeu.sysalmacen.modelo.VentaDetalle;

public interface IVentaDetalleService extends ICrudGenericoService<VentaDetalle, Long>{
    VentaDetalleDTO saveD(VentaDetalleDTO.VentaDetalleCADTO dto);
}
