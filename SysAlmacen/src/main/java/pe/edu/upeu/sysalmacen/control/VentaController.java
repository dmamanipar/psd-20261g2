package pe.edu.upeu.sysalmacen.control;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upeu.sysalmacen.dtos.VentaDTO;
import pe.edu.upeu.sysalmacen.mappers.VentaMapper;
import pe.edu.upeu.sysalmacen.modelo.Venta;
import pe.edu.upeu.sysalmacen.servicio.IVentaService;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ventas")
public class VentaController {
    private final IVentaService ventaService;
    private final VentaMapper ventaMapper;

    @GetMapping
    public ResponseEntity<List<VentaDTO>> findAll() {
        List<VentaDTO> list = ventaMapper.toDTOs(ventaService.findAll());
        return ResponseEntity.ok(list);
    }
    @GetMapping("/{id}")
    public ResponseEntity<VentaDTO> findById(@PathVariable("id") Long id) {
        Venta obj = ventaService.findById(id);
        return ResponseEntity.ok(ventaMapper.toDTO(obj));
    }
    @PostMapping
    public ResponseEntity<Void> save(@Valid @RequestBody VentaDTO.VentaCADTO dto) {
        VentaDTO obj = ventaService.saveD(dto);
        URI location =
                ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(
                        obj.getIdVenta()).toUri();
        return ResponseEntity.created(location).build();
    }



}
