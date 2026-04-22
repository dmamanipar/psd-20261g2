package pe.edu.upeu.sysalmacen.servicio.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.sysalmacen.dtos.VentaDetalleDTO;
import pe.edu.upeu.sysalmacen.mappers.VentaDetalleMapper;
import pe.edu.upeu.sysalmacen.modelo.*;
import pe.edu.upeu.sysalmacen.repositorio.ICrudGenericoRepository;
import pe.edu.upeu.sysalmacen.repositorio.IProductoRepository;
import pe.edu.upeu.sysalmacen.repositorio.IVentaDetalleRepository;
import pe.edu.upeu.sysalmacen.repositorio.IVentaRepository;
import pe.edu.upeu.sysalmacen.servicio.IVentaDetalleService;

@Service
@Transactional
@RequiredArgsConstructor
public class VentaDetalleServiceImp extends CrudGenericoServiceImp<VentaDetalle, Long> implements IVentaDetalleService {

    private final IVentaDetalleRepository repo;
    private final IProductoRepository productoRepository;
    private final IVentaRepository ventaRepository;
    private final VentaDetalleMapper ventaDetalleMapper;
    @Override
    protected ICrudGenericoRepository<VentaDetalle, Long> getRepo() {return repo; }

    @Override
    public VentaDetalleDTO saveD(VentaDetalleDTO.VentaDetalleCADTO dto) {
        VentaDetalle to = ventaDetalleMapper.toEntityFromCADTO(dto);
        Producto toA = productoRepository.findById(dto.producto()).orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        Venta toB = ventaRepository.findById(dto.venta()).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        to.setProducto(toA);
        to.setVenta(toB);

        VentaDetalle regGuardado = repo.save(to);
        return ventaDetalleMapper.toDTO(regGuardado);
    }
}
