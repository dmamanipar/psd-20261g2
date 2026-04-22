package pe.edu.upeu.sysalmacen.servicio.impl;

import pe.edu.upeu.sysalmacen.excepciones.CustomResponse;
import pe.edu.upeu.sysalmacen.excepciones.ModelNotFoundException;
import pe.edu.upeu.sysalmacen.repositorio.ICrudGenericoRepository;
import pe.edu.upeu.sysalmacen.servicio.ICrudGenericoService;

import java.time.LocalDateTime;
import java.util.List;

public abstract class CrudGenericoServiceImp<T,ID> implements ICrudGenericoService<T,ID> {
    protected abstract ICrudGenericoRepository<T, ID> getRepo();
    private static final String ID_NOT_FOUND="ID NOT FOUND: ";
    @Override
    public T save(T t) {
        return getRepo().save(t);
    }
    @Override
    public T update(ID id, T t) {
        getRepo().findById(id).orElseThrow(() -> new ModelNotFoundException(ID_NOT_FOUND + id));
        return getRepo().save(t);
    }
    @Override
    public List<T> findAll() {
        return getRepo().findAll();
    }
    @Override
    public T findById(ID id) {
        return getRepo().findById(id).orElseThrow(() -> new ModelNotFoundException(ID_NOT_FOUND + id));
    }
    @Override
    public CustomResponse delete(ID id) {
        getRepo().findById(id).orElseThrow(() -> new ModelNotFoundException(ID_NOT_FOUND + id));
        getRepo().deleteById(id);
        CustomResponse cer=new CustomResponse();
        cer.setStatusCode(200);
        cer.setDatetime(LocalDateTime.now());
        cer.setMessage("true");
        cer.setDetails("Todo Ok");
        return cer;
    }
}
