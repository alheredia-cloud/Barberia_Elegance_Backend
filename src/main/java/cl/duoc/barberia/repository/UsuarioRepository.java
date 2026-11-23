package cl.duoc.barberia.repository;

import cl.duoc.barberia.model.Rol;
import cl.duoc.barberia.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    boolean existsByRut(String rut);
    boolean existsByRutAndIdNot(String rut, Long id);
    long countByRol(Rol rol);
}
