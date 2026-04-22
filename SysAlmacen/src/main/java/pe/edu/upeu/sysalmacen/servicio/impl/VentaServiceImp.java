package pe.edu.upeu.sysalmacen.servicio.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.sysalmacen.dtos.VentaDTO;
import pe.edu.upeu.sysalmacen.mappers.VentaMapper;
import pe.edu.upeu.sysalmacen.modelo.*;
import pe.edu.upeu.sysalmacen.repositorio.*;
import pe.edu.upeu.sysalmacen.servicio.IVentaService;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class VentaServiceImp extends CrudGenericoServiceImp<Venta, Long> implements IVentaService {
    private final IVentaRepository repo;
    private final IClienteRepository clienteRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IVentCarritoRepository ventCarritoRepository;
    private final IProductoRepository productoRepository;
    private final IVentaDetalleRepository ventaDetalleRepository;
    private final VentaMapper ventaMapper;
    @Override
    protected ICrudGenericoRepository<Venta, Long> getRepo() {
        return repo;
    }

    @Override
    public VentaDTO saveD(VentaDTO.VentaCADTO dto) {
        Venta to = ventaMapper.toEntityFromCADTO(dto);
        Cliente toA = clienteRepository.findById(dto.cliente()).orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
        Usuario toB = usuarioRepository.findById(dto.usuario()).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        to.setCliente(toA);
        to.setUsuario(toB);

        Venta regGuardado = repo.save(to);

        List<VentCarrito> carrito = ventCarritoRepository.listaCarritoCliente(regGuardado.getCliente().getDniruc());
        if (regGuardado.getIdVenta() != 0) {
            for (VentCarrito car : carrito) {
                VentaDetalle vd = VentaDetalle.builder()
                        .venta(regGuardado)
                        .producto(productoRepository.findById(car.getProducto().getIdProducto()).get())
                        .cantidad(car.getCantidad())
                        .descuento(0.0)
                        .pu(car.getPunitario())
                        .subtotal(car.getPtotal())
                        .build();
                ventaDetalleRepository.save(vd);
            }
        }
        ventCarritoRepository.deleteByDniruc(regGuardado.getCliente().getDniruc());

        return ventaMapper.toDTO(regGuardado);
    }
}
