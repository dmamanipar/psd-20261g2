package pe.edu.upeu.sysalmacen.servicio.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.sysalmacen.modelo.Marca;
import pe.edu.upeu.sysalmacen.repositorio.ICrudGenericoRepository;
import pe.edu.upeu.sysalmacen.repositorio.IMarcaRepository;
import pe.edu.upeu.sysalmacen.servicio.IMarcaService;

@Service
@Transactional
@RequiredArgsConstructor
public class MarcaServiceImp extends CrudGenericoServiceImp<Marca, Long> implements IMarcaService {
    private final IMarcaRepository repo;

    @Override
    protected ICrudGenericoRepository<Marca, Long> getRepo(){
        return repo;
    }

    @Override
    public Long periodoIdMax() {
        return repo.maxID().get();
    }
}
