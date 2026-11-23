package cl.duoc.barberia.repository;

import cl.duoc.barberia.model.Producto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @EntityGraph(attributePaths = "categoria")
    List<Producto> findByActivoTrueOrderByIdAsc();

    @EntityGraph(attributePaths = "categoria")
    List<Producto> findByActivoTrueAndCategoriaIdOrderByIdAsc(Long categoriaId);

    @EntityGraph(attributePaths = "categoria")
    Optional<Producto> findByIdAndActivoTrue(Long id);

    boolean existsByCategoriaIdAndActivoTrue(Long categoriaId);

    long countByActivoTrue();

    /** Valor total del inventario fisico (precio * stock). */
    @Query("select coalesce(sum(p.precio * p.stock), 0) from Producto p where p.activo = true and p.stock is not null")
    Long valorInventario();
}
