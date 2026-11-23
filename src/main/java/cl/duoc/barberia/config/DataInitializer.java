package cl.duoc.barberia.config;

import cl.duoc.barberia.model.*;
import cl.duoc.barberia.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;

/** Carga datos iniciales solo si la base de datos esta vacia. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final CitaRepository citaRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            usuarioRepository.save(usuario("Administrador", "12345678-5", "admin@barberia.cl", "admin123", Rol.ADMIN));
            usuarioRepository.save(usuario("Valentina Vendedora", "11111111-1", "vendedor@barberia.cl", "vendedor123", Rol.VENDEDOR));
            usuarioRepository.save(usuario("Juan Perez", "22222222-2", "cliente@barberia.cl", "cliente123", Rol.CLIENTE));
            log.info("Usuarios iniciales creados (admin, vendedor, cliente)");
        }

        if (categoriaRepository.count() == 0) {
            Categoria cortes = categoriaRepository.save(categoria("Cortes", "Cortes de cabello", true));
            Categoria barba = categoriaRepository.save(categoria("Barba", "Perfilado y afeitado", true));
            Categoria combos = categoriaRepository.save(categoria("Combos", "Paquetes de servicios", true));
            Categoria tratamientos = categoriaRepository.save(categoria("Tratamientos", "Cuidado facial y capilar", true));
            Categoria productos = categoriaRepository.save(categoria("Productos", "Productos fisicos para el cuidado masculino", false));

            productoRepository.save(producto("Corte Clasico", "Corte tradicional con tijera y maquina, lavado incluido.", 15000, null, "/imagenes/corte clasico.jpe", cortes));
            productoRepository.save(producto("Barba Completa", "Perfilado, recorte y toalla caliente.", 12000, null, "/imagenes/Barba completa.jpe", barba));
            productoRepository.save(producto("Afeitado Clasico", "Afeitado con navaja y espuma tradicional.", 10000, null, "/imagenes/afeitado clasico.jpe", barba));
            productoRepository.save(producto("Combo Elegance", "Incluye: 1. Pulido de la barba a eleccion, 2. Corte de cabello incluido y masajes faciales.", 22000, null, "/imagenes/afeitado imperial.jpg", combos));
            productoRepository.save(producto("Tratamiento Facial", "Limpieza profunda e hidratacion.", 18000, null, "/imagenes/tratamiento facial.jpe", tratamientos));
            productoRepository.save(producto("Cera Modeladora", "Fijacion media con acabado mate.", 8000, 15, "/imagenes/pomada modeladora.jpe", productos));
            productoRepository.save(producto("Aceite de Barba", "Hidrata y suaviza la barba. 30 ml.", 9500, 20, "/imagenes/aceite de barba.jpe", productos));
            log.info("Catalogo inicial creado");
        }

        if (citaRepository.count() == 0) {
            LocalDate manana = LocalDate.now(clock).plusDays(1);
            citaRepository.save(Cita.builder().nombreCliente("Juan Perez").fecha(manana).hora(LocalTime.of(10, 0))
                    .servicios("Corte Clasico, Barba Completa").notas("Prefiere corte degradado").build());
            citaRepository.save(Cita.builder().nombreCliente("Carlos Ruiz").fecha(manana).hora(LocalTime.of(11, 30))
                    .servicios("Combo Elegance").notas("Cita de cumpleaños").build());
        }
    }

    private Usuario usuario(String nombre, String rut, String email, String pass, Rol rol) {
        return Usuario.builder().nombre(nombre).rut(rut).email(email)
                .password(passwordEncoder.encode(pass)).rol(rol).build();
    }

    private static Categoria categoria(String nombre, String desc, boolean servicio) {
        return Categoria.builder().nombre(nombre).descripcion(desc).esServicio(servicio).build();
    }

    private static Producto producto(String nombre, String desc, int precio, Integer stock, String img, Categoria c) {
        return Producto.builder().nombre(nombre).descripcion(desc).precio(precio).stock(stock).imagen(img).categoria(c).build();
    }
}
