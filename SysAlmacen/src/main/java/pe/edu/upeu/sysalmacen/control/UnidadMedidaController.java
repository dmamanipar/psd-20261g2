package pe.edu.upeu.sysalmacen.control;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upeu.sysalmacen.dtos.UnidadMedidaDTO;
import pe.edu.upeu.sysalmacen.mappers.UnidadMedidaMapper;
import pe.edu.upeu.sysalmacen.modelo.UnidadMedida;
import pe.edu.upeu.sysalmacen.servicio.IUnidadMedidaService;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/unidadmedidas")
public class UnidadMedidaController {
    private final IUnidadMedidaService unidadMedidaService;
    private final UnidadMedidaMapper unidadMedidaMapper;

    @GetMapping
    public ResponseEntity<List<UnidadMedidaDTO>> findAll() {
        List<UnidadMedidaDTO> list = unidadMedidaMapper.toDTOs(unidadMedidaService.findAll());
        return ResponseEntity.ok(list);
    }
    @GetMapping("/{id}")
    public ResponseEntity<UnidadMedidaDTO> findById(@PathVariable("id") Long id) {
        UnidadMedida obj = unidadMedidaService.findById(id);
        return ResponseEntity.ok(unidadMedidaMapper.toDTO(obj));
    }
    @PostMapping
    public ResponseEntity<Void> save(@Valid @RequestBody UnidadMedidaDTO dto) {
        UnidadMedida obj = unidadMedidaService.save(unidadMedidaMapper.toEntity(dto));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(obj.getIdUnidad())
                        .toUri();
        return ResponseEntity.created(location).build();
    }
    @PutMapping("/{id}")
    public ResponseEntity<UnidadMedidaDTO> update(@Valid @PathVariable("id") Long id,
                                                  @RequestBody UnidadMedidaDTO dto) {
        dto.setIdUnidad(id);
        UnidadMedida obj = unidadMedidaService.update(id, unidadMedidaMapper.toEntity(dto));
        return ResponseEntity.ok(unidadMedidaMapper.toDTO(obj));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        unidadMedidaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

