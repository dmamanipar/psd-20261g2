package pe.edu.upeu.sysalmacen.servicio.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.sysalmacen.modelo.Cliente;
import pe.edu.upeu.sysalmacen.repositorio.IClienteRepository;
import pe.edu.upeu.sysalmacen.repositorio.ICrudGenericoRepository;
import pe.edu.upeu.sysalmacen.servicio.IClienteService;

@Service
@Transactional
@RequiredArgsConstructor
public class ClienteServiceImp extends CrudGenericoServiceImp<Cliente, String> implements IClienteService {

    private final IClienteRepository repo;
    @Override
    protected ICrudGenericoRepository<Cliente, String> getRepo(){
        return repo;
    }

}
