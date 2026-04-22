package pe.edu.upeu.sysalmacen.control;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upeu.sysalmacen.dtos.ClienteDTO;
import pe.edu.upeu.sysalmacen.mappers.ClienteMapper;
import pe.edu.upeu.sysalmacen.modelo.Cliente;
import pe.edu.upeu.sysalmacen.servicio.IClienteService;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/clientes")
public class ClienteController {
    private final IClienteService clienteService;
    private final ClienteMapper clienteMapper;

    @GetMapping
    public ResponseEntity<List<ClienteDTO>> findAll() {
        List<ClienteDTO> list = clienteMapper.toDTOs(clienteService.findAll());
        return ResponseEntity.ok(list);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ClienteDTO> findById(@PathVariable("id") String id) {
        Cliente obj = clienteService.findById(id);
        return ResponseEntity.ok(clienteMapper.toDTO(obj));
    }
    @PostMapping
    public ResponseEntity<Void> save(@Valid @RequestBody ClienteDTO dto) {
        Cliente obj = clienteService.save(clienteMapper.toEntity(dto));
        URI location =
                ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(
                        obj.getDniruc()).toUri();
        return ResponseEntity.created(location).build();
    }
    @PutMapping("/{id}")
    public ResponseEntity<ClienteDTO> update(@Valid @PathVariable("id") String
                                                       dniruc, @RequestBody ClienteDTO dto) {
        dto.setDniruc(dniruc);
        Cliente obj = clienteService.update(dniruc, clienteMapper.toEntity(dto));
        return ResponseEntity.ok(clienteMapper.toDTO(obj));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        clienteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

