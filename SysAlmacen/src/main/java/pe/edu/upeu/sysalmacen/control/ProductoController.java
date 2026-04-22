package pe.edu.upeu.sysalmacen.control;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upeu.sysalmacen.dtos.ProductoDTO;
import pe.edu.upeu.sysalmacen.mappers.ProductoMapper;
import pe.edu.upeu.sysalmacen.modelo.Producto;
import pe.edu.upeu.sysalmacen.servicio.IProductoService;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/productos")
//@CrossOrigin("*")
public class ProductoController {

    private final IProductoService productoService;
    private final ProductoMapper productoMapper;

    @GetMapping
    public ResponseEntity<List<ProductoDTO>> findAll() {
        List<ProductoDTO> list = productoMapper.toDTOs(productoService.findAll());
        return ResponseEntity.ok(list);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> findById(@PathVariable("id") Long id) {
        Producto obj = productoService.findById(id);
        return ResponseEntity.ok(productoMapper.toDTO(obj));
    }

    @PostMapping
    public ResponseEntity<Void> save(@Valid @RequestBody ProductoDTO.ProductoCADto dto) {
        ProductoDTO obj = productoService.saveD(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(obj.getIdProducto()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoDTO> update(@Valid @RequestBody ProductoDTO.ProductoCADto dto, @PathVariable("id") Long id) {
        ProductoDTO obj = productoService.updateD(dto, id);
        return ResponseEntity.ok(obj);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        productoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pageable")
    public ResponseEntity<org.springframework.data.domain.Page<ProductoDTO>> listPage(Pageable pageable){
        Page<ProductoDTO> page = productoService.listaPage(pageable).map(e -> productoMapper.toDTO(e));
        return ResponseEntity.ok(page);
    }
}
