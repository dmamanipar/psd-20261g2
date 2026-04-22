package pe.edu.upeu.sysalmacen.servicio.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.upeu.sysalmacen.modelo.Acceso;
import pe.edu.upeu.sysalmacen.repositorio.IAccesoRepository;
import pe.edu.upeu.sysalmacen.repositorio.ICrudGenericoRepository;
import pe.edu.upeu.sysalmacen.servicio.IAccesoService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccesoServiceImp extends CrudGenericoServiceImp<Acceso, Long> implements IAccesoService {
    private final IAccesoRepository repo;
    @Override
    protected ICrudGenericoRepository<Acceso, Long> getRepo() {
        return repo;
    }
    @Override
    public List<Acceso> getAccesoByUser(String username) {
        return repo.getAccesoByUser(username);
    }
}
