package pe.edu.upeu.sysalmacen.servicio;

import pe.edu.upeu.sysalmacen.modelo.Acceso;

import java.util.List;

public interface IAccesoService {
    List<Acceso> getAccesoByUser(String username);
}
