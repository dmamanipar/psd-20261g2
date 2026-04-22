package pe.edu.upeu.sysalmacen.repositorio;

import pe.edu.upeu.sysalmacen.modelo.Usuario;

import java.util.Optional;

public interface IUsuarioRepository extends ICrudGenericoRepository<Usuario, Long>{

    Optional<Usuario> findOneByUser(String user);
}
