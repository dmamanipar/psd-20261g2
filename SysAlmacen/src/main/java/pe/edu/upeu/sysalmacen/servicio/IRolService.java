package pe.edu.upeu.sysalmacen.servicio;

import pe.edu.upeu.sysalmacen.modelo.Rol;

import java.util.Optional;

public interface IRolService extends ICrudGenericoService<Rol, Long>{
    public Optional<Rol> getByNombre(Rol.RolNombre rolNombre);
}