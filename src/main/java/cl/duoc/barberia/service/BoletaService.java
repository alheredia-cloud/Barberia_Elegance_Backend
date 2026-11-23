package cl.duoc.barberia.service;

import cl.duoc.barberia.dto.BoletaDto.*;
import cl.duoc.barberia.exception.BusinessException;
import cl.duoc.barberia.exception.ResourceNotFoundException;
import cl.duoc.barberia.model.*;
import cl.duoc.barberia.repository.BoletaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BoletaService {

    private final BoletaRepository boletaRepository;
    private final ProductoService productoService;
    private final UsuarioService usuarioService;

    /**
     * Checkout: valida productos y stock, congela precios, calcula el total en el servidor
     * y descuenta stock de los productos fisicos. Todo en una sola transaccion.
     */
    @Transactional
    public BoletaResponse crear(BoletaRequest req, String emailUsuario) {
        Usuario usuario = usuarioService.buscarPorEmail(emailUsuario);

        // Agrupa items repetidos del carrito por producto
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        req.items().forEach(i -> cantidades.merge(i.productoId(), i.cantidad(), Integer::sum));

        Boleta boleta = Boleta.builder()
                .usuario(usuario)
                .nombreCliente(req.nombreCliente().trim())
                .rutCliente(req.rutCliente())
                .direccion(req.direccion())
                .metodoPago(req.metodoPago())
                .total(0)
                .build();

        int total = 0;
        for (Map.Entry<Long, Integer> e : cantidades.entrySet()) {
            Producto producto = productoService.buscarActivo(e.getKey());
            int cantidad = e.getValue();
            productoService.descontarStock(producto, cantidad);
            int subtotal = producto.getPrecio() * cantidad;
            total += subtotal;
            boleta.agregarDetalle(DetalleBoleta.builder()
                    .producto(producto)
                    .cantidad(cantidad)
                    .precioUnitario(producto.getPrecio())
                    .subtotal(subtotal)
                    .build());
        }
        boleta.setTotal(total);
        return toResponse(boletaRepository.save(boleta));
    }

    @Transactional(readOnly = true)
    public List<BoletaResponse> listar() {
        return boletaRepository.findAllByOrderByFechaDesc().stream().map(BoletaService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<BoletaResponse> listarPorUsuario(String email) {
        return boletaRepository.findByUsuarioEmailIgnoreCaseOrderByFechaDesc(email)
                .stream().map(BoletaService::toResponse).toList();
    }

    /** ADMIN y VENDEDOR ven cualquier boleta; un CLIENTE solo las propias. */
    @Transactional(readOnly = true)
    public BoletaResponse obtener(Long id, String email, boolean esPersonal) {
        Boleta b = buscar(id);
        if (!esPersonal && !b.getUsuario().getEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("La boleta no pertenece al usuario");
        }
        return toResponse(b);
    }

    /** Anular una boleta repone el stock; no se puede reactivar una anulada. */
    @Transactional
    public BoletaResponse cambiarEstado(Long id, EstadoBoleta nuevo) {
        Boleta b = buscar(id);
        if (b.getEstado() == nuevo) {
            return toResponse(b);
        }
        if (b.getEstado() == EstadoBoleta.ANULADA) {
            throw new BusinessException("Una boleta anulada no puede reactivarse", HttpStatus.CONFLICT);
        }
        b.getDetalles().forEach(d -> productoService.reponerStock(d.getProducto(), d.getCantidad()));
        b.setEstado(nuevo);
        return toResponse(b);
    }

    @Transactional
    public void eliminar(Long id) {
        boletaRepository.delete(buscar(id));
    }

    Boleta buscar(Long id) {
        return boletaRepository.findWithDetallesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Boleta", id));
    }

    static BoletaResponse toResponse(Boleta b) {
        List<DetalleResponse> detalles = b.getDetalles().stream()
                .map(d -> new DetalleResponse(d.getId(), d.getProducto().getId(), d.getProducto().getNombre(),
                        d.getProducto().isServicio(), d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal()))
                .toList();
        boolean requiereCita = detalles.stream().anyMatch(DetalleResponse::esServicio);
        return new BoletaResponse(b.getId(), b.getFecha(), b.getUsuario().getEmail(), b.getNombreCliente(),
                b.getRutCliente(), b.getDireccion(), b.getMetodoPago(), b.getTotal(), b.getEstado(),
                requiereCita, detalles);
    }
}
