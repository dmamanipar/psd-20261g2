package pe.edu.upeu.sysalmacen.servicio.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.sysalmacen.modelo.UnidadMedida;
import pe.edu.upeu.sysalmacen.repositorio.ICrudGenericoRepository;
import pe.edu.upeu.sysalmacen.repositorio.IUnidadMedidaRepository;
import pe.edu.upeu.sysalmacen.servicio.IUnidadMedidaService;

@Service
@Transactional
@RequiredArgsConstructor
public class UnidadMedidaServiceImp extends CrudGenericoServiceImp<UnidadMedida, Long> implements IUnidadMedidaService {

    private final IUnidadMedidaRepository repo;
    @Override
    protected ICrudGenericoRepository<UnidadMedida, Long> getRepo() {
        return repo;
    }
}
