package cl.duoc.barberia.controller;

import cl.duoc.barberia.dto.BoletaDto.BoletaRequest;
import cl.duoc.barberia.dto.BoletaDto.BoletaResponse;
import cl.duoc.barberia.dto.BoletaDto.EstadoRequest;
import cl.duoc.barberia.service.BoletaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/boletas")
@RequiredArgsConstructor
@Tag(name = "Boletas", description = "Ordenes de compra: checkout del cliente y consulta para ADMIN/VENDEDOR")
public class BoletaController {

    private final BoletaService boletaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Crear boleta (checkout)",
            description = "Calcula el total en el servidor y descuenta stock de productos fisicos")
    @ApiResponse(responseCode = "201", description = "Compra registrada")
    @ApiResponse(responseCode = "409", description = "Stock insuficiente")
    public BoletaResponse crear(@Valid @RequestBody BoletaRequest req, Authentication auth) {
        return boletaService.crear(req, auth.getName());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    @Operation(summary = "Listar todas las boletas (ADMIN, VENDEDOR)")
    @ApiResponse(responseCode = "403", description = "Rol CLIENTE no autorizado")
    public List<BoletaResponse> listar() {
        return boletaService.listar();
    }

    @GetMapping("/mis-boletas")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Historial de compras del usuario autenticado")
    public List<BoletaResponse> misBoletas(Authentication auth) {
        return boletaService.listarPorUsuario(auth.getName());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Detalle de boleta", description = "ADMIN/VENDEDOR ven cualquiera; CLIENTE solo las propias")
    @ApiResponse(responseCode = "403", description = "La boleta no pertenece al usuario")
    @ApiResponse(responseCode = "404", description = "No encontrada")
    public BoletaResponse obtener(@PathVariable Long id, Authentication auth) {
        boolean esPersonal = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_VENDEDOR"));
        return boletaService.obtener(id, auth.getName(), esPersonal);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar estado (ADMIN)", description = "Anular una boleta repone el stock")
    public BoletaResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRequest req) {
        return boletaService.cambiarEstado(id, req.estado());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar boleta (ADMIN)")
    public void eliminar(@PathVariable Long id) {
        boletaService.eliminar(id);
    }
}
