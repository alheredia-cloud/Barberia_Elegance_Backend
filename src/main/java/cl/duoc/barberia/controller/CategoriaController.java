package cl.duoc.barberia.controller;

import cl.duoc.barberia.dto.CatalogoDto.CategoriaRequest;
import cl.duoc.barberia.dto.CatalogoDto.CategoriaResponse;
import cl.duoc.barberia.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorias", description = "Categorias de productos y servicios")
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    @SecurityRequirements
    @Operation(summary = "Listar categorias (publico)")
    public List<CategoriaResponse> listar() {
        return categoriaService.listar();
    }

    @GetMapping("/{id}")
    @SecurityRequirements
    @Operation(summary = "Obtener categoria por id (publico)")
    @ApiResponse(responseCode = "404", description = "No encontrada")
    public CategoriaResponse obtener(@PathVariable Long id) {
        return categoriaService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear categoria (ADMIN)")
    @ApiResponse(responseCode = "201", description = "Creada")
    @ApiResponse(responseCode = "409", description = "Nombre duplicado")
    public CategoriaResponse crear(@Valid @RequestBody CategoriaRequest req) {
        return categoriaService.crear(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar categoria (ADMIN)")
    public CategoriaResponse actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest req) {
        return categoriaService.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar categoria (ADMIN)", description = "Falla con 409 si tiene productos asociados")
    @ApiResponse(responseCode = "204", description = "Eliminada")
    public void eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
    }
}
