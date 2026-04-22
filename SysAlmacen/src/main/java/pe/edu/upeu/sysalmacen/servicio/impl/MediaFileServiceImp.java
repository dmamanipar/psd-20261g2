package pe.edu.upeu.sysalmacen.servicio.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.upeu.sysalmacen.modelo.MediaFile;
import pe.edu.upeu.sysalmacen.repositorio.ICrudGenericoRepository;
import pe.edu.upeu.sysalmacen.repositorio.IMediaFileRepository;
import pe.edu.upeu.sysalmacen.servicio.IMediaFileService;

@Service
@RequiredArgsConstructor
public class MediaFileServiceImp extends CrudGenericoServiceImp<MediaFile, Long> implements IMediaFileService {
    private final IMediaFileRepository repo;

    @Override
    protected ICrudGenericoRepository<MediaFile, Long> getRepo() {
        return repo;
    }
}