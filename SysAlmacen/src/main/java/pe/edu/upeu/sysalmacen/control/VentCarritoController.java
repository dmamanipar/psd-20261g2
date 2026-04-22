package pe.edu.upeu.sysalmacen.control;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upeu.sysalmacen.dtos.VentCarritoDTO;
import pe.edu.upeu.sysalmacen.mappers.VentCarritoMapper;
import pe.edu.upeu.sysalmacen.modelo.VentCarrito;
import pe.edu.upeu.sysalmacen.servicio.IVentCarritoService;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ventcarritos")
public class VentCarritoController {
    private final IVentCarritoService ventCarritoService;
    private final VentCarritoMapper ventCarritoMapper;
    @GetMapping
    public ResponseEntity<List<VentCarritoDTO>> findAll() {
        List<VentCarritoDTO> list = ventCarritoMapper.toDTOs(ventCarritoService.findAll());
        return ResponseEntity.ok(list);
    }
    @GetMapping("/{id}")
    public ResponseEntity<VentCarritoDTO> findById(@PathVariable("id") Long id) {
        VentCarrito obj = ventCarritoService.findById(id);
        return ResponseEntity.ok(ventCarritoMapper.toDTO(obj));
    }
    @PostMapping
    public ResponseEntity<Void> save(@Valid @RequestBody VentCarritoDTO.VentCarritoCADTO dto) {
        VentCarritoDTO obj = ventCarritoService.saveD(dto);
        URI location =
                ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(
                        obj.getIdCarrito()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<VentCarritoDTO> update(@Valid @RequestBody VentCarritoDTO.VentCarritoCADTO dto, @PathVariable("id") Long id) {
        VentCarritoDTO obj = ventCarritoService.updateD(dto, id);
        return ResponseEntity.ok(obj);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        ventCarritoService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/list/{dniruc}")
    public ResponseEntity<List<VentCarritoDTO>> listaCarritoCliente(@PathVariable("dniruc") String dniruc) {
        List<VentCarritoDTO> list = ventCarritoMapper.toDTOs(ventCarritoService.listaCarritoCliente(dniruc));
        return ResponseEntity.ok(list);
    }
}
