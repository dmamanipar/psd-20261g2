package pe.edu.upeu.sysalmacen.servicio.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.sysalmacen.dtos.ProductoDTO;
import pe.edu.upeu.sysalmacen.dtos.report.ProdMasVendidosDTO;
import pe.edu.upeu.sysalmacen.mappers.ProductoMapper;
import pe.edu.upeu.sysalmacen.modelo.Categoria;
import pe.edu.upeu.sysalmacen.modelo.Marca;
import pe.edu.upeu.sysalmacen.modelo.Producto;
import pe.edu.upeu.sysalmacen.modelo.UnidadMedida;
import pe.edu.upeu.sysalmacen.repositorio.*;
import pe.edu.upeu.sysalmacen.servicio.IProductoService;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductoServiceImp extends CrudGenericoServiceImp<Producto, Long> implements IProductoService {

    private final DataSource dataSource;

    private final IProductoRepository repo;
    private final ProductoMapper productoMapper;
    private final ICategoriaRepository categoriaRepository;
    private final IMarcaRepository marcaRepository;
    private final IUnidadMedidaRepository unidadMedidaRepository;
    @Override
    protected ICrudGenericoRepository<Producto, Long> getRepo() {
        return repo;
    }


    @Override
    public ProductoDTO saveD(ProductoDTO.ProductoCADto dto) {
        Producto producto = productoMapper.toEntityFromCADTO(dto);

        Categoria categoria = categoriaRepository.findById(dto.categoria())
                .orElseThrow(() -> new EntityNotFoundException("Categoria no encontrada"));
        Marca marca = marcaRepository.findById(dto.marca())
                .orElseThrow(() -> new EntityNotFoundException("Marca no encontrada"));
        UnidadMedida unidadMedida = unidadMedidaRepository.findById(dto.unidadMedida())
                .orElseThrow(() -> new EntityNotFoundException("Unidad de medida no encontrada"));

        producto.setCategoria(categoria);
        producto.setMarca(marca);
        producto.setUnidadMedida(unidadMedida);

        Producto productoGuardado = repo.save(producto);
        return productoMapper.toDTO(productoGuardado);
    }

    @Override
    public ProductoDTO updateD(ProductoDTO.ProductoCADto dto, Long id) {
        Producto producto = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        Producto productox = productoMapper.toEntityFromCADTO(dto);
        productox.setIdProducto(producto.getIdProducto());

        Categoria categoria = categoriaRepository.findById(dto.categoria())
                .orElseThrow(() -> new EntityNotFoundException("Categoria no encontrada"));
        Marca marca = marcaRepository.findById(dto.marca())
                .orElseThrow(() -> new EntityNotFoundException("Marca no encontrada"));
        UnidadMedida unidadMedida = unidadMedidaRepository.findById(dto.unidadMedida())
                .orElseThrow(() -> new EntityNotFoundException("Unidad de medida no encontrada"));

        productox.setCategoria(categoria);
        productox.setMarca(marca);
        productox.setUnidadMedida(unidadMedida);

        Producto productoActualizado = repo.save(productox);
        return productoMapper.toDTO(productoActualizado);
    }

    public List<ProdMasVendidosDTO> obtenerProductosMasVendidos(){
        return repo.findProductosMasVendidos();
    }

    public byte[] generateReport() throws JRException, SQLException, IOException {
        HashMap<String, Object> param = new HashMap<>();
        param.put("txt_title", "SysAlmacen DMP");

        File jrxmlFile = new ClassPathResource("/reports/venta_productos.jrxml").getFile();
        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlFile.getPath());
        // Llenar el informe
        JasperPrint jprint = JasperFillManager.fillReport(jasperReport, param, dataSource.getConnection());
        byte[] pdfBytes = JasperExportManager.exportReportToPdf(jprint);
        String projectRootPath = System.getProperty("user.dir"); // Obtiene la ruta raíz del proyecto
        String outputPath = projectRootPath + "/reporte.pdf"; // Ruta del archivo dentro de la carpeta raíz
        JasperExportManager.exportReportToPdfFile(jprint, outputPath);
        // Exportar el informe a un byte[]
        return pdfBytes;
    }

    public Page<Producto> listaPage(Pageable pageable){
        return repo.findAll(pageable);
    }
}
