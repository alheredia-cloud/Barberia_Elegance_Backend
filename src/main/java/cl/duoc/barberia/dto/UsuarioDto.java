package cl.duoc.barberia.dto;

import cl.duoc.barberia.model.Rol;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

/** DTOs de gestion de usuarios (solo ADMIN). */
public final class UsuarioDto {

    private UsuarioDto() {
    }

    public record UsuarioRequest(
            @Schema(example = "Pedro Vendedor") @NotBlank @Size(max = 100) String nombre,
            @Schema(example = "22222222-2")
            @NotBlank @Pattern(regexp = "^\\d{7,8}-[\\dkK]$", message = "Formato de RUT invalido (ej: 12345678-9)") String rut,
            @Schema(example = "pedro@barberia.cl") @NotBlank @Email @Size(max = 120) String email,
            @Schema(example = "clave123", description = "Obligatoria al crear, opcional al actualizar")
            @Size(min = 6, max = 50, message = "La contraseña debe tener entre 6 y 50 caracteres") String password,
            @Schema(example = "VENDEDOR") @NotNull Rol rol,
            Boolean activo
    ) {
    }

    public record CambioRolRequest(@NotNull Rol rol) {
    }

    public record UsuarioResponse(
            Long id,
            String nombre,
            String rut,
            String email,
            Rol rol,
            boolean activo,
            LocalDateTime fechaRegistro
    ) {
    }
}
