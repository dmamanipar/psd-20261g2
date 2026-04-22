package pe.edu.upeu.sysalmacen.control;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upeu.sysalmacen.dtos.CategoriaDTO;
import pe.edu.upeu.sysalmacen.mappers.CategoriaMapper;
import pe.edu.upeu.sysalmacen.modelo.Categoria;
import pe.edu.upeu.sysalmacen.servicio.ICategoriaService;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/categorias")
public class CategoriaController {
    private final ICategoriaService categoriaService;
    private final CategoriaMapper categoriaMapper;
    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> findAll() {
        List<CategoriaDTO> list = categoriaMapper.toDTOs(categoriaService.findAll());
        return ResponseEntity.ok(list);
    }
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDTO> findById(@PathVariable("id") Long id) {
        Categoria obj = categoriaService.findById(id);
        return ResponseEntity.ok(categoriaMapper.toDTO(obj));
    }
    @PostMapping
    public ResponseEntity<Void> save(@Valid @RequestBody CategoriaDTO dto) {
        Categoria obj = categoriaService.save(categoriaMapper.toEntity(dto));
        URI location =
                ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(
                        obj.getIdCategoria()).toUri();
        return ResponseEntity.created(location).build();
    }
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaDTO> update(@Valid @PathVariable("id") Long
                                                       id, @RequestBody CategoriaDTO dto) {
        dto.setIdCategoria(id);
        Categoria obj = categoriaService.update(id, categoriaMapper.toEntity(dto));
        return ResponseEntity.ok(categoriaMapper.toDTO(obj));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        categoriaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
