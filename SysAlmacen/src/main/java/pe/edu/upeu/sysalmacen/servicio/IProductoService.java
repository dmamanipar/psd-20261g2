package pe.edu.upeu.sysalmacen.servicio;

import net.sf.jasperreports.engine.JRException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.edu.upeu.sysalmacen.dtos.ProductoDTO;
import pe.edu.upeu.sysalmacen.dtos.report.ProdMasVendidosDTO;
import pe.edu.upeu.sysalmacen.modelo.Producto;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public interface IProductoService extends ICrudGenericoService<Producto, Long>{
    ProductoDTO saveD(ProductoDTO.ProductoCADto dto);

    ProductoDTO updateD(ProductoDTO.ProductoCADto dto, Long id);

    public List<ProdMasVendidosDTO> obtenerProductosMasVendidos();

    byte[] generateReport() throws JRException, SQLException, IOException;

    Page<Producto> listaPage(Pageable pageable);
}
