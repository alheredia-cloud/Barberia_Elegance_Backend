package cl.duoc.barberia.repository;

import cl.duoc.barberia.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    List<Mensaje> findAllByOrderByFechaDesc();
    long countByLeidoFalse();
}
