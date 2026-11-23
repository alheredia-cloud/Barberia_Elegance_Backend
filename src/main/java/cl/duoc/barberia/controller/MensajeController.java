package cl.duoc.barberia.controller;

import cl.duoc.barberia.dto.AgendaDto.MensajeRequest;
import cl.duoc.barberia.dto.AgendaDto.MensajeResponse;
import cl.duoc.barberia.service.MensajeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mensajes")
@RequiredArgsConstructor
@Tag(name = "Mensajes", description = "Formulario de contacto y bandeja del administrador")
public class MensajeController {

    private final MensajeService mensajeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirements
    @Operation(summary = "Enviar mensaje de contacto (publico)")
    public MensajeResponse crear(@Valid @RequestBody MensajeRequest req) {
        return mensajeService.crear(req);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Bandeja de mensajes (ADMIN)")
    public List<MensajeResponse> listar() {
        return mensajeService.listar();
    }

    @PatchMapping("/{id}/leido")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Marcar mensaje como leido / no leido (ADMIN)")
    public MensajeResponse marcarLeido(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean leido) {
        return mensajeService.marcarLeido(id, leido);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar mensaje (ADMIN)")
    public void eliminar(@PathVariable Long id) {
        mensajeService.eliminar(id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Vaciar bandeja (ADMIN)")
    public void eliminarTodos() {
        mensajeService.eliminarTodos();
    }
}
