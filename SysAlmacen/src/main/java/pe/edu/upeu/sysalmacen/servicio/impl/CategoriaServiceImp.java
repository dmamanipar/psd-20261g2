package pe.edu.upeu.sysalmacen.servicio.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.sysalmacen.modelo.Categoria;
import pe.edu.upeu.sysalmacen.repositorio.ICategoriaRepository;
import pe.edu.upeu.sysalmacen.repositorio.ICrudGenericoRepository;
import pe.edu.upeu.sysalmacen.servicio.ICategoriaService;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoriaServiceImp extends CrudGenericoServiceImp<Categoria,Long>  implements ICategoriaService {

    private final ICategoriaRepository repo;
    @Override
    protected ICrudGenericoRepository<Categoria, Long> getRepo(){
        return repo;
    }

}
