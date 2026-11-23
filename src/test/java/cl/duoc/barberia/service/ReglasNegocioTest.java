package cl.duoc.barberia.service;

import cl.duoc.barberia.exception.BusinessException;
import cl.duoc.barberia.model.Categoria;
import cl.duoc.barberia.model.Producto;
import cl.duoc.barberia.repository.BoletaRepository;
import cl.duoc.barberia.repository.CitaRepository;
import cl.duoc.barberia.repository.ProductoRepository;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/** Pruebas unitarias de la logica de negocio, sin base de datos. */
class ReglasNegocioTest {

    // ---------- RUT ----------
    @Test
    void rutValidoEInvalido() {
        assertThat(RutUtil.esValido("12345678-5")).isTrue();
        assertThat(RutUtil.esValido("11111111-1")).isTrue();
        assertThat(RutUtil.esValido("12345678-9")).isFalse();
        assertThat(RutUtil.esValido("1234-5")).isFalse();
        assertThat(RutUtil.esValido(null)).isFalse();
    }

    // ---------- Stock ----------
    private final ProductoService productoService =
            new ProductoService(mock(ProductoRepository.class), mock(CategoriaService.class));

    private static Producto producto(boolean servicio, Integer stock) {
        Categoria c = Categoria.builder().nombre("c").esServicio(servicio).build();
        return Producto.builder().nombre("p").precio(1000).stock(stock).categoria(c).build();
    }

    @Test
    void descontarStockDeProductoFisico() {
        Producto p = producto(false, 5);
        productoService.descontarStock(p, 3);
        assertThat(p.getStock()).isEqualTo(2);
    }

    @Test
    void stockInsuficienteLanzaExcepcion() {
        Producto p = producto(false, 1);
        assertThatThrownBy(() -> productoService.descontarStock(p, 2))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Stock insuficiente");
    }

    @Test
    void serviciosTienenStockIlimitado() {
        Producto p = producto(true, null);
        productoService.descontarStock(p, 100);
        assertThat(p.getStock()).isNull();
    }

    // ---------- Agenda ----------
    private final Clock reloj = Clock.fixed(Instant.parse("2026-11-23T15:00:00Z"), ZoneId.of("America/Santiago"));
    private final CitaService citaService = new CitaService(mock(CitaRepository.class),
            mock(BoletaRepository.class), mock(UsuarioService.class), reloj);

    @Test
    void citaEnElPasadoEsRechazada() {
        assertThatThrownBy(() -> citaService.validarHorario(LocalDate.of(2026, 11, 22), LocalTime.of(10, 0)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("pasado");
    }

    @Test
    void citaFueraDeHorarioEsRechazada() {
        assertThatThrownBy(() -> citaService.validarHorario(LocalDate.of(2026, 11, 25), LocalTime.of(21, 0)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("horario");
    }

    @Test
    void citaFueraDeBloqueEsRechazada() {
        assertThatThrownBy(() -> citaService.validarHorario(LocalDate.of(2026, 11, 25), LocalTime.of(10, 15)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("30 minutos");
    }

    @Test
    void citaValidaEsAceptada() {
        citaService.validarHorario(LocalDate.of(2026, 11, 25), LocalTime.of(10, 30));
    }
}
