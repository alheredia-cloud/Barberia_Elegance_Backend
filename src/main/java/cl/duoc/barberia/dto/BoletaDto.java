package cl.duoc.barberia.dto;

import cl.duoc.barberia.model.EstadoBoleta;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

/** DTOs de boletas (ordenes de compra). */
public final class BoletaDto {

    private BoletaDto() {
    }

    public record ItemRequest(
            @Schema(example = "1") @NotNull Long productoId,
            @Schema(example = "1") @NotNull @Min(value = 1, message = "La cantidad minima es 1") Integer cantidad
    ) {
    }

    /** El total NO se recibe desde el cliente: se calcula en el servidor con los precios de la BD. */
    public record BoletaRequest(
            @Schema(example = "Juan Perez") @NotBlank @Size(max = 100) String nombreCliente,
            @Schema(example = "11111111-1") @Size(max = 12) String rutCliente,
            @Schema(example = "Av. Siempre Viva 123") @Size(max = 200) String direccion,
            @Schema(example = "transferencia") @NotBlank @Size(max = 30) String metodoPago,
            @NotEmpty(message = "El carrito no puede estar vacio") List<@Valid ItemRequest> items
    ) {
    }

    public record EstadoRequest(@NotNull EstadoBoleta estado) {
    }

    public record DetalleResponse(
            Long id,
            Long productoId,
            String producto,
            boolean esServicio,
            Integer cantidad,
            Integer precioUnitario,
            Integer subtotal
    ) {
    }

    public record BoletaResponse(
            Long id,
            LocalDateTime fecha,
            String emailUsuario,
            String nombreCliente,
            String rutCliente,
            String direccion,
            String metodoPago,
            Integer total,
            EstadoBoleta estado,
            boolean requiereCita,
            List<DetalleResponse> detalles
    ) {
    }
}
