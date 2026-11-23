package cl.duoc.barberia.controller;

import cl.duoc.barberia.dto.AgendaDto.CitaRequest;
import cl.duoc.barberia.dto.AgendaDto.CitaResponse;
import cl.duoc.barberia.service.CitaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/citas")
@RequiredArgsConstructor
@Tag(name = "Citas", description = "Agenda de horas para servicios")
public class CitaController {

    private final CitaService citaService;

    @GetMapping("/disponibilidad")
    @SecurityRequirements
    @Operation(summary = "Horas ocupadas en una fecha (publico)")
    public List<LocalTime> disponibilidad(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return citaService.horasOcupadas(fecha);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Agendar cita", description = "Valida fecha futura, horario 09:00-20:00, bloques de 30 min y hora libre")
    @ApiResponse(responseCode = "201", description = "Cita agendada")
    @ApiResponse(responseCode = "400", description = "Fecha u hora invalida")
    @ApiResponse(responseCode = "409", description = "Hora ya reservada")
    public CitaResponse crear(@Valid @RequestBody CitaRequest req, Authentication auth) {
        return citaService.crear(req, auth.getName());
    }

    @GetMapping("/mis-citas")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Citas del usuario autenticado")
    public List<CitaResponse> misCitas(Authentication auth) {
        return citaService.listarPorUsuario(auth.getName());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Agenda completa (ADMIN)", description = "Filtro opcional por fecha (yyyy-MM-dd)")
    public List<CitaResponse> listar(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return citaService.listar(fecha);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener cita (ADMIN)")
    public CitaResponse obtener(@PathVariable Long id) {
        return citaService.obtener(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reprogramar cita (ADMIN)")
    public CitaResponse actualizar(@PathVariable Long id, @Valid @RequestBody CitaRequest req) {
        return citaService.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cancelar cita (ADMIN)")
    public void eliminar(@PathVariable Long id) {
        citaService.eliminar(id);
    }
}
