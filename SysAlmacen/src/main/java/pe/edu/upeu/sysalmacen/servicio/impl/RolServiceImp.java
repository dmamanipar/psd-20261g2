package pe.edu.upeu.sysalmacen.servicio.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.sysalmacen.modelo.Rol;
import pe.edu.upeu.sysalmacen.repositorio.ICrudGenericoRepository;
import pe.edu.upeu.sysalmacen.repositorio.IRolRepository;
import pe.edu.upeu.sysalmacen.servicio.IRolService;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class RolServiceImp extends CrudGenericoServiceImp<Rol, Long> implements IRolService {
    private final IRolRepository repo;
    @Override
    protected ICrudGenericoRepository<Rol, Long> getRepo() {
        return repo;
    }
    @Override
    public Optional<Rol> getByNombre(Rol.RolNombre rolNombre) {
        return repo.findByNombre(rolNombre);
    }

}
