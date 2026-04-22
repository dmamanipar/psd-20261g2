package pe.edu.upeu.sysalmacen.servicio.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.sysalmacen.dtos.VentCarritoDTO;
import pe.edu.upeu.sysalmacen.mappers.VentCarritoMapper;
import pe.edu.upeu.sysalmacen.modelo.*;
import pe.edu.upeu.sysalmacen.repositorio.ICrudGenericoRepository;
import pe.edu.upeu.sysalmacen.repositorio.IProductoRepository;
import pe.edu.upeu.sysalmacen.repositorio.IUsuarioRepository;
import pe.edu.upeu.sysalmacen.repositorio.IVentCarritoRepository;
import pe.edu.upeu.sysalmacen.servicio.IVentCarritoService;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class VentCarritoServiceImp extends CrudGenericoServiceImp<VentCarrito, Long> implements IVentCarritoService {
    private final IVentCarritoRepository repo;
    private final VentCarritoMapper ventCarritoMapper;
    private final IProductoRepository productoRepository;
    private final IUsuarioRepository usuarioRepository;
    @Override
    protected ICrudGenericoRepository<VentCarrito, Long> getRepo() {
        return repo;
    }


    @Override
    public VentCarritoDTO saveD(VentCarritoDTO.VentCarritoCADTO dto) {
        VentCarrito to = ventCarritoMapper.toEntityFromCADTO(dto);
        System.out.println("Llegooooooooo");
        Producto toA = productoRepository.findById(dto.producto()).orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        Usuario toB = usuarioRepository.findById(dto.usuario()).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        to.setProducto(toA);
        to.setUsuario(toB);

        VentCarrito regGuardado = repo.save(to);
        return ventCarritoMapper.toDTO(regGuardado);
    }

    @Override
    public VentCarritoDTO updateD(VentCarritoDTO.VentCarritoCADTO dto, Long id) {
        VentCarrito to = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Carrito no encontrado"));

        VentCarrito toX = ventCarritoMapper.toEntityFromCADTO(dto);
        toX.setIdCarrito(to.getIdCarrito());

        Producto toA = productoRepository.findById(dto.producto()).orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        Usuario toB = usuarioRepository.findById(dto.usuario()).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        toX.setProducto(toA);
        toX.setUsuario(toB);

        VentCarrito productoActualizado = repo.save(toX);
        return ventCarritoMapper.toDTO(productoActualizado);
    }

    @Override
    public List<VentCarrito> listaCarritoCliente(String dniruc){
      return repo.listaCarritoCliente(dniruc);
    }

    @Override
    public void deleteCarAll(String dniruc) {
        this.repo.deleteByDniruc(dniruc);
    }
}
