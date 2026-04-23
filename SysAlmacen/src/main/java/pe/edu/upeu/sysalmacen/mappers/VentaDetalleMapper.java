package pe.edu.upeu.sysalmacen.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.edu.upeu.sysalmacen.dtos.VentaDetalleDTO;
import pe.edu.upeu.sysalmacen.modelo.VentaDetalle;

@Mapper(componentModel = "spring", uses = {ProductoMapper.class})
public interface VentaDetalleMapper  extends GenericMapper<VentaDetalleDTO, VentaDetalle>{
    @Override
    @Mapping(target = "venta", ignore = true)
    VentaDetalleDTO toDTO(VentaDetalle entity);

    @Override
    @Mapping(target = "venta", ignore = true)
    VentaDetalle toEntity(VentaDetalleDTO dto);

    @Mapping(target = "venta", ignore = true)  // Ignoramos aquí porque asignamos
    @Mapping(target = "producto", ignore = true)
    VentaDetalle toEntityFromCADTO(VentaDetalleDTO.VentaDetalleCADTO ventaDetalleCADTO);
}
