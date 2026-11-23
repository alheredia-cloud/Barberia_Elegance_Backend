package cl.duoc.barberia.controller;

import cl.duoc.barberia.dto.UsuarioDto.CambioRolRequest;
import cl.duoc.barberia.dto.UsuarioDto.UsuarioRequest;
import cl.duoc.barberia.dto.UsuarioDto.UsuarioResponse;
import cl.duoc.barberia.exception.BusinessException;
import cl.duoc.barberia.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Usuarios", description = "Administracion de cuentas y roles (solo ADMIN)")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @Operation(summary = "Listar usuarios")
    @ApiResponse(responseCode = "403", description = "Solo ADMIN")
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por id")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear usuario con cualquier rol (ej. VENDEDOR)")
    @ApiResponse(responseCode = "201", description = "Creado")
    @ApiResponse(responseCode = "409", description = "Correo o RUT duplicado")
    public UsuarioResponse crear(@Valid @RequestBody UsuarioRequest req) {
        if (req.password() == null || req.password().isBlank()) {
            throw new BusinessException("La contraseña es obligatoria al crear un usuario");
        }
        return usuarioService.crear(req);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario", description = "La contraseña es opcional; si viene vacia se mantiene")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest req, Authentication auth) {
        return usuarioService.actualizar(id, req, auth.getName());
    }

    @PatchMapping("/{id}/rol")
    @Operation(summary = "Cambiar rol de un usuario")
    public UsuarioResponse cambiarRol(@PathVariable Long id, @Valid @RequestBody CambioRolRequest req, Authentication auth) {
        return usuarioService.cambiarRol(id, req.rol(), auth.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar usuario", description = "No permite eliminarse a si mismo ni al ultimo ADMIN")
    public void eliminar(@PathVariable Long id, Authentication auth) {
        usuarioService.eliminar(id, auth.getName());
    }
}
