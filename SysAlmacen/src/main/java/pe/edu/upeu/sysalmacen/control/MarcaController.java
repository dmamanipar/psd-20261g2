package pe.edu.upeu.sysalmacen.control;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upeu.sysalmacen.dtos.MarcaDTO;
import pe.edu.upeu.sysalmacen.excepciones.CustomResponse;
import pe.edu.upeu.sysalmacen.mappers.MarcaMapper;
import pe.edu.upeu.sysalmacen.modelo.Marca;
import pe.edu.upeu.sysalmacen.servicio.IMarcaService;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/marcas")
//@CrossOrigin("*")
public class MarcaController {
    private final IMarcaService marcaService;
    private final MarcaMapper marcaMapper;


    @GetMapping
    public ResponseEntity<List<MarcaDTO>> findAll() {
        List<MarcaDTO> list = marcaMapper.toDTOs(marcaService.findAll());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MarcaDTO> findById(@PathVariable("id") Long id) {
        Marca obj = marcaService.findById(id);
        return ResponseEntity.ok(marcaMapper.toDTO(obj));
    }
    /*
    * Otra forma de llamar return ResponseEntity.created(location).build();
    * */
    @PostMapping
    public ResponseEntity<CustomResponse> save(@Valid @RequestBody MarcaDTO dto) {
        Marca obj = marcaService.save(marcaMapper.toEntity(dto));
        //URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(obj.getIdMarca()).toUri();
        return ResponseEntity.ok(new CustomResponse(200,LocalDateTime.now(), (obj!=null?"true":"false"), String.valueOf(obj.getIdMarca())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MarcaDTO> update(@Valid @PathVariable("id") Long id, @RequestBody MarcaDTO dto) {
        dto.setIdMarca(id);
        Marca obj = marcaService.update(id, marcaMapper.toEntity(dto));
        return ResponseEntity.ok(marcaMapper.toDTO(obj));
    }
    /*
    * Otra forma de retornar - return ResponseEntity.noContent().build();
    * */
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse> delete(@PathVariable("id") Long id) {
        CustomResponse operacion= marcaService.delete(id);
        return ResponseEntity.ok(operacion);
    }

    @GetMapping("/buscarmaxid")
    public ResponseEntity<Long> getMarcaMaxId() {
        Long idMax = marcaService.periodoIdMax();
        return ResponseEntity.ok(idMax);
    }

    /*@GetMapping("/hateoas/{id}")
    public EntityModel<MarcaDTO> findByIdHateoas(@PathVariable("id") Long id) {
        EntityModel<MarcaDTO> resource = EntityModel.of(mapperUtil.map(service.findById(id), PatientDTO.class));

        //generar link informativo
        WebMvcLinkBuilder link1 = linkTo(methodOn(this.getClass()).findById(id));
        WebMvcLinkBuilder link2 = linkTo(methodOn(MedicController.class).findAll());

        resource.add(link1.withRel("patient-self-info"));
        resource.add(link2.withRel("all-medic-info"));

        return resource;
    }*/
}
