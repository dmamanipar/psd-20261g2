package pe.edu.upeu.sysalmacen.mappers;

import org.mapstruct.Mapper;
import pe.edu.upeu.sysalmacen.dtos.MarcaDTO;
import pe.edu.upeu.sysalmacen.modelo.Marca;

@Mapper(componentModel = "spring")
public interface MarcaMapper extends GenericMapper<MarcaDTO, Marca> {

}
