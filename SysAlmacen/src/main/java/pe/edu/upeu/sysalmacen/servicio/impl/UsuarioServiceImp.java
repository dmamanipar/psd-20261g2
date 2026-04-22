package pe.edu.upeu.sysalmacen.servicio.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.sysalmacen.dtos.UsuarioDTO;
import pe.edu.upeu.sysalmacen.excepciones.ModelNotFoundException;
import pe.edu.upeu.sysalmacen.mappers.UsuarioMapper;
import pe.edu.upeu.sysalmacen.modelo.Rol;
import pe.edu.upeu.sysalmacen.modelo.Usuario;
import pe.edu.upeu.sysalmacen.modelo.UsuarioRol;
import pe.edu.upeu.sysalmacen.repositorio.ICrudGenericoRepository;
import pe.edu.upeu.sysalmacen.repositorio.IUsuarioRepository;
import pe.edu.upeu.sysalmacen.servicio.IRolService;
import pe.edu.upeu.sysalmacen.servicio.IUsuarioRolService;
import pe.edu.upeu.sysalmacen.servicio.IUsuarioService;

import java.nio.CharBuffer;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UsuarioServiceImp extends CrudGenericoServiceImp<Usuario, Long> implements IUsuarioService {
    private final IUsuarioRepository repo;

    private final IRolService rolService;
    private final IUsuarioRolService iurService;

    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper userMapper;

    @Override
    protected ICrudGenericoRepository<Usuario, Long> getRepo() {
        return repo;
    }



    @Override
    public UsuarioDTO login(UsuarioDTO.CredencialesDto credentialsDto) {
        Usuario user = repo.findOneByUser(credentialsDto.user())
                .orElseThrow(() -> new ModelNotFoundException("Unknown user", HttpStatus.NOT_FOUND));

        if (passwordEncoder.matches(CharBuffer.wrap(credentialsDto.clave()), user.getClave())) {
            return userMapper.toDTO(user);
        }

        throw new ModelNotFoundException("Invalid password", HttpStatus.BAD_REQUEST);
    }



    @Override
    public UsuarioDTO register(UsuarioDTO.UsuarioCrearDto userDto) {
        Optional<Usuario> optionalUser = repo.findOneByUser(userDto.user());
        if (optionalUser.isPresent()) {
            throw new ModelNotFoundException("Login already exists", HttpStatus.BAD_REQUEST);
        }
        Usuario user = userMapper.toEntityFromCADTO(userDto);
        user.setClave(passwordEncoder.encode(CharBuffer.wrap(userDto.clave())));
        Usuario savedUser = repo.save(user);
        Rol r;
        switch (userDto.rol()){
            case "ADMIN":{
                r=rolService.getByNombre(Rol.RolNombre.ADMIN).orElse(null);
            } break;
            case "DBA":{
                r=rolService.getByNombre(Rol.RolNombre.DBA).orElse(null);
            } break;
            default:{
                r=rolService.getByNombre(Rol.RolNombre.USER).orElse(null);
            } break;
        }

        /*UsuarioRol u=new UsuarioRol();
        u.setRol(r);
        u.setUsuario(savedUser);
        iurService.save(u);
        */

        iurService.save(UsuarioRol.builder()
                .usuario(savedUser)
                .rol(r)
                .build());
        return userMapper.toDTO(savedUser);
    }
}

