package pe.edu.upeu.sysalmacen.servicio;

import pe.edu.upeu.sysalmacen.modelo.Marca;

public interface IMarcaService extends ICrudGenericoService<Marca, Long>{
    Long periodoIdMax();
}
