package pe.edu.upeu.sysalmacen.repositorio;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.sysalmacen.modelo.VentCarrito;

import java.util.List;

public interface IVentCarritoRepository extends ICrudGenericoRepository<VentCarrito, Long> {
    @Query("SELECT v FROM VentCarrito v WHERE v.dniruc = :dniruc")
    List<VentCarrito> listaCarritoCliente(@Param("dniruc") String dniruc);

    void deleteByDniruc(String dniruc);
}
