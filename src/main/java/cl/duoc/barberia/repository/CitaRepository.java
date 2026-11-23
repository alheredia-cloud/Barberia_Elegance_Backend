package cl.duoc.barberia.repository;

import cl.duoc.barberia.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findAllByOrderByFechaAscHoraAsc();
    List<Cita> findByUsuarioEmailIgnoreCaseOrderByFechaAscHoraAsc(String email);
    List<Cita> findByFechaOrderByHoraAsc(LocalDate fecha);
    boolean existsByFechaAndHora(LocalDate fecha, LocalTime hora);
    boolean existsByFechaAndHoraAndIdNot(LocalDate fecha, LocalTime hora, Long id);
}
