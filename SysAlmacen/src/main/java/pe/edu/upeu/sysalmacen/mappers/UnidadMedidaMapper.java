package pe.edu.upeu.sysalmacen.mappers;

import org.mapstruct.Mapper;
import pe.edu.upeu.sysalmacen.dtos.UnidadMedidaDTO;
import pe.edu.upeu.sysalmacen.modelo.UnidadMedida;

@Mapper(componentModel = "spring")
public interface UnidadMedidaMapper extends
        GenericMapper<UnidadMedidaDTO, UnidadMedida>
{
}
