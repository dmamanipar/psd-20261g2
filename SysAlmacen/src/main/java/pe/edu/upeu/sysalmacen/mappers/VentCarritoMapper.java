package pe.edu.upeu.sysalmacen.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.edu.upeu.sysalmacen.dtos.VentCarritoDTO;
import pe.edu.upeu.sysalmacen.modelo.VentCarrito;

@Mapper(componentModel = "spring")
public interface VentCarritoMapper  extends GenericMapper<VentCarritoDTO, VentCarrito>{
    @Mapping(target = "producto", ignore = true)  // Ignoramos aquí porque asignamos
    @Mapping(target = "usuario", ignore = true)
    VentCarrito toEntityFromCADTO(VentCarritoDTO.VentCarritoCADTO ventCarritoCrearDTO);

}
