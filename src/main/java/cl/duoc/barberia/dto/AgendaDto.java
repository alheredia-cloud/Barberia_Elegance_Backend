package cl.duoc.barberia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** DTOs de citas, mensajes de contacto y dashboard. */
public final class AgendaDto {

    private AgendaDto() {
    }

    public record CitaRequest(
            @Schema(example = "Juan Perez") @NotBlank @Size(max = 100) String nombreCliente,
            @Schema(example = "2026-12-15") @NotNull LocalDate fecha,
            @Schema(example = "10:00", type = "string") @NotNull LocalTime hora,
            @Schema(example = "[\"Corte Clasico\"]") @NotEmpty List<@NotBlank String> servicios,
            @Schema(example = "Prefiere degradado") @Size(max = 500) String notas,
            @Schema(example = "1", description = "Boleta asociada (opcional)") Long boletaId
    ) {
    }

    public record CitaResponse(
            Long id,
            String nombreCliente,
            String emailUsuario,
            LocalDate fecha,
            LocalTime hora,
            List<String> servicios,
            String notas,
            Long boletaId
    ) {
    }

    public record MensajeRequest(
            @Schema(example = "Ana Diaz") @NotBlank @Size(max = 100) String nombre,
            @Schema(example = "ana@correo.cl") @NotBlank @Email @Size(max = 120) String email,
            @Schema(example = "Quisiera saber el horario del sabado") @NotBlank @Size(max = 1000) String mensaje
    ) {
    }

    public record MensajeResponse(Long id, String nombre, String email, String mensaje, LocalDateTime fecha, boolean leido) {
    }

    public record ResumenDashboard(
            long totalUsuarios,
            long totalClientes,
            long totalProductos,
            long valorInventario,
            long totalBoletas,
            long totalVentas,
            long totalCitas,
            long mensajesNoLeidos
    ) {
    }
}
