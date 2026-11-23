package cl.duoc.barberia.dto;

import cl.duoc.barberia.model.Rol;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** DTOs del modulo de autenticacion. */
public final class AuthDto {

    private AuthDto() {
    }

    public record LoginRequest(
            @Schema(example = "admin@barberia.cl") @NotBlank @Email String email,
            @Schema(example = "admin123") @NotBlank String password
    ) {
    }

    public record RegisterRequest(
            @Schema(example = "Juan Perez") @NotBlank @Size(max = 100) String nombre,
            @Schema(example = "11111111-1")
            @NotBlank @Pattern(regexp = "^\\d{7,8}-[\\dkK]$", message = "Formato de RUT invalido (ej: 12345678-9)") String rut,
            @Schema(example = "juan@correo.cl") @NotBlank @Email @Size(max = 120) String email,
            @Schema(example = "clave123") @NotBlank @Size(min = 6, max = 50, message = "La contraseña debe tener entre 6 y 50 caracteres") String password
    ) {
    }

    public record RefreshRequest(@NotBlank String refreshToken) {
    }

    public record AuthResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresIn,
            UsuarioInfo usuario
    ) {
    }

    public record UsuarioInfo(Long id, String nombre, String email, String rut, Rol rol) {
    }
}
