package cl.duoc.barberia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/** DTOs de categorias y productos. */
public final class CatalogoDto {

    private CatalogoDto() {
    }

    public record CategoriaRequest(
            @Schema(example = "Cortes") @NotBlank @Size(max = 60) String nombre,
            @Schema(example = "Cortes de cabello") @Size(max = 255) String descripcion,
            @Schema(example = "true", description = "true = servicio (stock ilimitado)") boolean esServicio
    ) {
    }

    public record CategoriaResponse(Long id, String nombre, String descripcion, boolean esServicio) {
    }

    public record ProductoRequest(
            @Schema(example = "Corte Clasico") @NotBlank @Size(max = 100) String nombre,
            @Schema(example = "Corte tradicional con tijera y maquina") @Size(max = 500) String descripcion,
            @Schema(example = "15000") @NotNull @Min(value = 0, message = "El precio no puede ser negativo") Integer precio,
            @Schema(example = "10", description = "Ignorado si la categoria es de servicios")
            @Min(value = 0, message = "El stock no puede ser negativo") Integer stock,
            @Schema(example = "/imagenes/corte clasico.jpe") @Size(max = 255) String imagen,
            @Schema(example = "1") @NotNull Long categoriaId
    ) {
    }

    public record ProductoResponse(
            Long id,
            String nombre,
            String descripcion,
            Integer precio,
            @Schema(description = "null cuando es un servicio (ilimitado)") Integer stock,
            boolean ilimitado,
            String imagen,
            Long categoriaId,
            String categoria,
            boolean esServicio
    ) {
    }
}
