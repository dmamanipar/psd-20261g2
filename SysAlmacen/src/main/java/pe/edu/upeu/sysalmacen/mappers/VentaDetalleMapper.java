package pe.edu.upeu.sysalmacen.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.edu.upeu.sysalmacen.dtos.VentaDetalleDTO;
import pe.edu.upeu.sysalmacen.modelo.VentaDetalle;

@Mapper(componentModel = "spring")
public interface VentaDetalleMapper  extends GenericMapper<VentaDetalleDTO, VentaDetalle>{
    @Mapping(target = "venta", ignore = true)  // Ignoramos aquí porque asignamos
    @Mapping(target = "producto", ignore = true)
    VentaDetalle toEntityFromCADTO(VentaDetalleDTO.VentaDetalleCADTO ventaDetalleCADTO);
}
