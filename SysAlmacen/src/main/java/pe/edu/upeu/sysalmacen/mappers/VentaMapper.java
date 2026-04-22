package pe.edu.upeu.sysalmacen.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.edu.upeu.sysalmacen.dtos.VentaDTO;
import pe.edu.upeu.sysalmacen.modelo.Venta;

@Mapper(componentModel = "spring")
public interface VentaMapper  extends GenericMapper<VentaDTO, Venta>{
    @Mapping(target = "cliente", ignore = true)  // Ignoramos aquí porque asignamos
    @Mapping(target = "usuario", ignore = true)
    Venta toEntityFromCADTO(VentaDTO.VentaCADTO ventaCADTO);
}
