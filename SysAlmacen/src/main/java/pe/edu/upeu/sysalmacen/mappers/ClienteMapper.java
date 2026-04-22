package pe.edu.upeu.sysalmacen.mappers;

import org.mapstruct.Mapper;
import pe.edu.upeu.sysalmacen.dtos.ClienteDTO;
import pe.edu.upeu.sysalmacen.modelo.Cliente;


@Mapper(componentModel = "spring")
public interface ClienteMapper extends GenericMapper<ClienteDTO, Cliente>{

}
