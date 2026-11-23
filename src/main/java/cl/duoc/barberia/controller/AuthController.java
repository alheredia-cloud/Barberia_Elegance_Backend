package cl.duoc.barberia.controller;

import cl.duoc.barberia.dto.AuthDto.*;
import cl.duoc.barberia.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticacion", description = "Login, registro y renovacion de tokens JWT")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Iniciar sesion", description = "Devuelve accessToken (30 min) y refreshToken (7 dias)")
    @ApiResponse(responseCode = "200", description = "Credenciales validas")
    @ApiResponse(responseCode = "401", description = "Credenciales invalidas o cuenta deshabilitada")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirements
    @Operation(summary = "Registrar cliente", description = "Crea una cuenta con rol CLIENTE y devuelve los tokens")
    @ApiResponse(responseCode = "201", description = "Cuenta creada")
    @ApiResponse(responseCode = "400", description = "Datos invalidos o RUT incorrecto")
    @ApiResponse(responseCode = "409", description = "Correo o RUT ya registrados")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.registrar(req);
    }

    @PostMapping("/refresh")
    @SecurityRequirements
    @Operation(summary = "Renovar tokens", description = "Recibe un refreshToken valido y entrega un nuevo par de tokens")
    @ApiResponse(responseCode = "200", description = "Tokens renovados")
    @ApiResponse(responseCode = "401", description = "Refresh token invalido o expirado")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest req) {
        return authService.refrescar(req);
    }

    @GetMapping("/me")
    @Operation(summary = "Perfil del usuario autenticado", description = "Permite al frontend validar la sesion al recargar la pagina")
    @ApiResponse(responseCode = "200", description = "Token valido")
    @ApiResponse(responseCode = "401", description = "Token ausente, invalido o expirado")
    public UsuarioInfo me(Authentication auth) {
        return authService.perfil(auth.getName());
    }
}
