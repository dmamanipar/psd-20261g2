package pe.edu.upeu.sysalmacen.mappers;

import org.mapstruct.Mapper;
import pe.edu.upeu.sysalmacen.dtos.CategoriaDTO;
import pe.edu.upeu.sysalmacen.modelo.Categoria;

@Mapper(componentModel = "spring")
public interface CategoriaMapper extends  GenericMapper<CategoriaDTO, Categoria> {
}
