package cl.duoc.barberia.service;

import cl.duoc.barberia.dto.CatalogoDto.CategoriaRequest;
import cl.duoc.barberia.dto.CatalogoDto.CategoriaResponse;
import cl.duoc.barberia.exception.BusinessException;
import cl.duoc.barberia.exception.ResourceNotFoundException;
import cl.duoc.barberia.model.Categoria;
import cl.duoc.barberia.repository.CategoriaRepository;
import cl.duoc.barberia.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll().stream().map(CategoriaService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse obtener(Long id) {
        return toResponse(buscar(id));
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest req) {
        if (categoriaRepository.existsByNombreIgnoreCase(req.nombre().trim())) {
            throw new BusinessException("Ya existe una categoria con ese nombre", HttpStatus.CONFLICT);
        }
        Categoria c = Categoria.builder()
                .nombre(req.nombre().trim())
                .descripcion(req.descripcion())
                .esServicio(req.esServicio())
                .build();
        return toResponse(categoriaRepository.save(c));
    }

    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest req) {
        Categoria c = buscar(id);
        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(req.nombre().trim(), id)) {
            throw new BusinessException("Ya existe una categoria con ese nombre", HttpStatus.CONFLICT);
        }
        c.setNombre(req.nombre().trim());
        c.setDescripcion(req.descripcion());
        c.setEsServicio(req.esServicio());
        return toResponse(c);
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria c = buscar(id);
        if (productoRepository.existsByCategoriaIdAndActivoTrue(id)) {
            throw new BusinessException("No se puede eliminar: la categoria tiene productos asociados", HttpStatus.CONFLICT);
        }
        categoriaRepository.delete(c);
    }

    Categoria buscar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", id));
    }

    static CategoriaResponse toResponse(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getDescripcion(), c.isEsServicio());
    }
}
