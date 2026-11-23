package cl.duoc.barberia.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/** Respuestas JSON para 401 (no autenticado / token invalido) y 403 (sin permisos). */
@Component
public class SecurityErrorHandlers {

    public AuthenticationEntryPoint entryPoint() {
        return (request, response, ex) -> {
            Object detalle = request.getAttribute(JwtAuthFilter.ATTR_ERROR);
            String mensaje = detalle != null ? detalle.toString() : "Debe iniciar sesion para acceder a este recurso";
            escribir(request, response, 401, "Unauthorized", mensaje);
        };
    }

    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) ->
                escribir(request, response, 403, "Forbidden", "No tiene permisos para realizar esta accion");
    }

    private static void escribir(HttpServletRequest req, HttpServletResponse res, int status, String error, String mensaje)
            throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String json = "{\"timestamp\":\"" + LocalDateTime.now() + "\",\"status\":" + status
                + ",\"error\":\"" + error + "\",\"mensaje\":\"" + mensaje
                + "\",\"ruta\":\"" + req.getRequestURI().replace("\"", "") + "\"}";
        res.getWriter().write(json);
    }
}
