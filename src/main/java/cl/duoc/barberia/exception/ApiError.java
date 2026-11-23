package cl.duoc.barberia.exception;

import java.time.LocalDateTime;
import java.util.Map;

/** Formato unico de error que recibe el frontend. */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String mensaje,
        String ruta,
        Map<String, String> campos
) {
    public static ApiError of(int status, String error, String mensaje, String ruta) {
        return new ApiError(LocalDateTime.now(), status, error, mensaje, ruta, null);
    }
}
