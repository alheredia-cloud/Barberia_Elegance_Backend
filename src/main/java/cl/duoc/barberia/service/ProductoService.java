package cl.duoc.barberia.service;

import cl.duoc.barberia.dto.CatalogoDto.ProductoRequest;
import cl.duoc.barberia.dto.CatalogoDto.ProductoResponse;
import cl.duoc.barberia.exception.BusinessException;
import cl.duoc.barberia.exception.ResourceNotFoundException;
import cl.duoc.barberia.model.Categoria;
import cl.duoc.barberia.model.Producto;
import cl.duoc.barberia.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaService categoriaService;

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(Long categoriaId) {
        List<Producto> productos = categoriaId == null
                ? productoRepository.findByActivoTrueOrderByIdAsc()
                : productoRepository.findByActivoTrueAndCategoriaIdOrderByIdAsc(categoriaId);
        return productos.stream().map(ProductoService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        return toResponse(buscarActivo(id));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest req) {
        Producto p = new Producto();
        aplicar(p, req);
        return toResponse(productoRepository.save(p));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest req) {
        Producto p = buscarActivo(id);
        aplicar(p, req);
        return toResponse(p);
    }

    /** Borrado logico para no romper el historial de boletas. */
    @Transactional
    public void eliminar(Long id) {
        Producto p = buscarActivo(id);
        p.setActivo(false);
    }

    /**
     * Regla de negocio: los servicios tienen stock ilimitado (null);
     * los productos fisicos requieren un stock numerico >= 0.
     */
    private void aplicar(Producto p, ProductoRequest req) {
        Categoria categoria = categoriaService.buscar(req.categoriaId());
        p.setNombre(req.nombre().trim());
        p.setDescripcion(req.descripcion());
        p.setPrecio(req.precio());
        p.setImagen(req.imagen());
        p.setCategoria(categoria);
        if (categoria.isEsServicio()) {
            p.setStock(null);
        } else {
            if (req.stock() == null) {
                throw new BusinessException("El stock es obligatorio para productos fisicos");
            }
            p.setStock(req.stock());
        }
    }

    /** Descuenta stock al vender. Los servicios no se descuentan. */
    void descontarStock(Producto p, int cantidad) {
        if (p.isServicio()) {
            return;
        }
        int disponible = p.getStock() == null ? 0 : p.getStock();
        if (disponible < cantidad) {
            throw new BusinessException("Stock insuficiente para '" + p.getNombre() + "'. Disponible: " + disponible,
                    HttpStatus.CONFLICT);
        }
        p.setStock(disponible - cantidad);
    }

    /** Devuelve stock cuando se anula una boleta. */
    void reponerStock(Producto p, int cantidad) {
        if (!p.isServicio()) {
            p.setStock((p.getStock() == null ? 0 : p.getStock()) + cantidad);
        }
    }

    Producto buscarActivo(Long id) {
        return productoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
    }

    static ProductoResponse toResponse(Producto p) {
        Categoria c = p.getCategoria();
        return new ProductoResponse(
                p.getId(), p.getNombre(), p.getDescripcion(), p.getPrecio(),
                p.getStock(), p.isServicio(), p.getImagen(),
                c.getId(), c.getNombre(), c.isEsServicio());
    }
}
