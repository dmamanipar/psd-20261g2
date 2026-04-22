package pe.edu.upeu.sysalmacen.repositorio;

import pe.edu.upeu.sysalmacen.modelo.Rol;

import java.util.Optional;

public interface IRolRepository extends ICrudGenericoRepository<Rol, Long>{
    Optional<Rol> findByNombre(Rol.RolNombre rolNombre);

    Optional<Rol> findByDescripcion(String nombre);

}
