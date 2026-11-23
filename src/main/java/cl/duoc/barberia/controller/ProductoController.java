package cl.duoc.barberia.controller;

import cl.duoc.barberia.dto.CatalogoDto.ProductoRequest;
import cl.duoc.barberia.dto.CatalogoDto.ProductoResponse;
import cl.duoc.barberia.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Catalogo de servicios y productos de la tienda")
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    @SecurityRequirements
    @Operation(summary = "Listar productos y servicios (publico)", description = "Filtro opcional por categoria")
    public List<ProductoResponse> listar(@Parameter(description = "Id de categoria") @RequestParam(required = false) Long categoriaId) {
        return productoService.listar(categoriaId);
    }

    @GetMapping("/{id}")
    @SecurityRequirements
    @Operation(summary = "Detalle de producto (publico)")
    @ApiResponse(responseCode = "404", description = "No encontrado")
    public ProductoResponse obtener(@PathVariable Long id) {
        return productoService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear producto (ADMIN)", description = "Si la categoria es de servicios el stock queda ilimitado")
    @ApiResponse(responseCode = "201", description = "Creado")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @ApiResponse(responseCode = "403", description = "Sin permisos")
    public ProductoResponse crear(@Valid @RequestBody ProductoRequest req) {
        return productoService.crear(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar producto (ADMIN)")
    @ApiResponse(responseCode = "404", description = "No encontrado")
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest req) {
        return productoService.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar producto (ADMIN)", description = "Borrado logico: se conserva para el historial de boletas")
    @ApiResponse(responseCode = "204", description = "Eliminado")
    public void eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
    }
}
