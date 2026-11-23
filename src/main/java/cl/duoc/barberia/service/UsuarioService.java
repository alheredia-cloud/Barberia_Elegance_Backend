package cl.duoc.barberia.service;

import cl.duoc.barberia.dto.UsuarioDto.UsuarioRequest;
import cl.duoc.barberia.dto.UsuarioDto.UsuarioResponse;
import cl.duoc.barberia.exception.BusinessException;
import cl.duoc.barberia.exception.ResourceNotFoundException;
import cl.duoc.barberia.model.Rol;
import cl.duoc.barberia.model.Usuario;
import cl.duoc.barberia.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(UsuarioService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return toResponse(buscar(id));
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario " + email + " no encontrado"));
    }

    /** Alta de usuario. Usado por el registro publico (rol CLIENTE) y por el ADMIN (cualquier rol). */
    @Transactional
    public Usuario registrar(String nombre, String rut, String email, String password, Rol rol) {
        String rutNorm = RutUtil.normalizar(rut);
        String emailNorm = email.trim().toLowerCase();
        validarRut(rutNorm);
        if (usuarioRepository.existsByEmailIgnoreCase(emailNorm)) {
            throw new BusinessException("El correo ya se encuentra registrado", HttpStatus.CONFLICT);
        }
        if (usuarioRepository.existsByRut(rutNorm)) {
            throw new BusinessException("El RUT ya se encuentra registrado", HttpStatus.CONFLICT);
        }
        if (password == null || password.length() < 6) {
            throw new BusinessException("La contraseña debe tener al menos 6 caracteres");
        }
        Usuario u = Usuario.builder()
                .nombre(nombre.trim())
                .rut(rutNorm)
                .email(emailNorm)
                .password(passwordEncoder.encode(password))
                .rol(rol)
                .build();
        return usuarioRepository.save(u);
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest req) {
        Usuario u = registrar(req.nombre(), req.rut(), req.email(), req.password(), req.rol());
        if (req.activo() != null) {
            u.setActivo(req.activo());
        }
        return toResponse(u);
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest req, String emailSolicitante) {
        Usuario u = buscar(id);
        String rutNorm = RutUtil.normalizar(req.rut());
        String emailNorm = req.email().trim().toLowerCase();
        validarRut(rutNorm);
        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(emailNorm, id)) {
            throw new BusinessException("El correo ya se encuentra registrado", HttpStatus.CONFLICT);
        }
        if (usuarioRepository.existsByRutAndIdNot(rutNorm, id)) {
            throw new BusinessException("El RUT ya se encuentra registrado", HttpStatus.CONFLICT);
        }
        protegerUltimoAdmin(u, req.rol(), req.activo() == null || req.activo(), emailSolicitante);
        u.setNombre(req.nombre().trim());
        u.setRut(rutNorm);
        u.setEmail(emailNorm);
        u.setRol(req.rol());
        if (req.activo() != null) {
            u.setActivo(req.activo());
        }
        if (req.password() != null && !req.password().isBlank()) {
            u.setPassword(passwordEncoder.encode(req.password()));
        }
        return toResponse(u);
    }

    @Transactional
    public UsuarioResponse cambiarRol(Long id, Rol rol, String emailSolicitante) {
        Usuario u = buscar(id);
        protegerUltimoAdmin(u, rol, u.isActivo(), emailSolicitante);
        u.setRol(rol);
        return toResponse(u);
    }

    @Transactional
    public void eliminar(Long id, String emailSolicitante) {
        Usuario u = buscar(id);
        if (u.getEmail().equalsIgnoreCase(emailSolicitante)) {
            throw new BusinessException("No puede eliminar su propia cuenta", HttpStatus.CONFLICT);
        }
        protegerUltimoAdmin(u, Rol.CLIENTE, false, emailSolicitante);
        usuarioRepository.delete(u);
    }

    /** Regla: el sistema nunca puede quedarse sin un administrador activo, ni el admin quitarse el rol a si mismo. */
    private void protegerUltimoAdmin(Usuario u, Rol nuevoRol, boolean nuevoActivo, String emailSolicitante) {
        if (u.getRol() != Rol.ADMIN) {
            return;
        }
        boolean pierdeAdmin = nuevoRol != Rol.ADMIN || !nuevoActivo;
        if (!pierdeAdmin) {
            return;
        }
        if (u.getEmail().equalsIgnoreCase(emailSolicitante)) {
            throw new BusinessException("No puede quitarse a si mismo el rol de administrador", HttpStatus.CONFLICT);
        }
        if (usuarioRepository.countByRol(Rol.ADMIN) <= 1) {
            throw new BusinessException("Debe existir al menos un administrador", HttpStatus.CONFLICT);
        }
    }

    private void validarRut(String rut) {
        if (!RutUtil.esValido(rut)) {
            throw new BusinessException("El RUT ingresado no es valido");
        }
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }

    public static UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getRut(), u.getEmail(),
                u.getRol(), u.isActivo(), u.getFechaRegistro());
    }
}
