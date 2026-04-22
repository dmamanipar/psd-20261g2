package pe.edu.upeu.sysalmacen.servicio;


import pe.edu.upeu.sysalmacen.modelo.UsuarioRol;

import java.util.List;

public interface IUsuarioRolService {
    List<UsuarioRol> findOneByUsuarioUser(String user);
    UsuarioRol save(UsuarioRol ur);
    //Usuario login(String usuario, String password);

}