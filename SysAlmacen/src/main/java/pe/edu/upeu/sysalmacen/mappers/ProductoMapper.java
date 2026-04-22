package pe.edu.upeu.sysalmacen.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.edu.upeu.sysalmacen.dtos.ProductoDTO;
import pe.edu.upeu.sysalmacen.modelo.Producto;

@Mapper(componentModel = "spring")
public interface ProductoMapper extends GenericMapper<ProductoDTO, Producto>{
    // Nuevo método para mapear desde ProductoCreateDTO a Producto
    @Mapping(target = "categoria", ignore = true)  // Ignoramos aquí porque asignamos la categoría en el servicio
    @Mapping(target = "marca", ignore = true)
    @Mapping(target = "unidadMedida", ignore = true)
    Producto toEntityFromCADTO(ProductoDTO.ProductoCADto productoCrearDTO);

}


