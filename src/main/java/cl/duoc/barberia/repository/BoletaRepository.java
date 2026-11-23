package cl.duoc.barberia.repository;

import cl.duoc.barberia.model.Boleta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BoletaRepository extends JpaRepository<Boleta, Long> {

    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.producto"})
    List<Boleta> findAllByOrderByFechaDesc();

    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.producto"})
    List<Boleta> findByUsuarioEmailIgnoreCaseOrderByFechaDesc(String email);

    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.producto"})
    Optional<Boleta> findWithDetallesById(Long id);

    @Query("select coalesce(sum(b.total), 0) from Boleta b where b.estado = cl.duoc.barberia.model.EstadoBoleta.PAGADA")
    Long totalVentas();
}
