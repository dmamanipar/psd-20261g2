package pe.edu.upeu.sysalmacen.servicio;

import pe.edu.upeu.sysalmacen.dtos.UsuarioDTO;
import pe.edu.upeu.sysalmacen.modelo.Usuario;

public interface IUsuarioService extends ICrudGenericoService<Usuario, Long>{
    public UsuarioDTO login(UsuarioDTO.CredencialesDto credentialsDto);
    public UsuarioDTO register(UsuarioDTO.UsuarioCrearDto userDto);
}