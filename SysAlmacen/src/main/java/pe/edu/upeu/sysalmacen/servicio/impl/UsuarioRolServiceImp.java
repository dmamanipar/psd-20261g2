package pe.edu.upeu.sysalmacen.servicio.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.upeu.sysalmacen.modelo.UsuarioRol;
import pe.edu.upeu.sysalmacen.repositorio.IUsuarioRolRepository;
import pe.edu.upeu.sysalmacen.servicio.IUsuarioRolService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioRolServiceImp implements IUsuarioRolService {
    private final IUsuarioRolRepository repo;
    @Override
    public List<UsuarioRol> findOneByUsuarioUser(String user) {
        return repo.findOneByUsuarioUser(user);
    }
    @Override
    public UsuarioRol save(UsuarioRol ur) {
        return repo.save(ur);
    }
}
