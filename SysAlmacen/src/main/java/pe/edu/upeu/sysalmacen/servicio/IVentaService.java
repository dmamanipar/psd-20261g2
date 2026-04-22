package pe.edu.upeu.sysalmacen.servicio;


import pe.edu.upeu.sysalmacen.dtos.VentaDTO;
import pe.edu.upeu.sysalmacen.modelo.Venta;

public interface IVentaService extends ICrudGenericoService<Venta, Long>{
    VentaDTO saveD(VentaDTO.VentaCADTO dto);
}
