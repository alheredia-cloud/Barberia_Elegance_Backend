package cl.duoc.barberia.service;

import cl.duoc.barberia.dto.AgendaDto.ResumenDashboard;
import cl.duoc.barberia.model.Rol;
import cl.duoc.barberia.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** KPIs del panel de administracion. */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final BoletaRepository boletaRepository;
    private final CitaRepository citaRepository;
    private final MensajeRepository mensajeRepository;

    @Transactional(readOnly = true)
    public ResumenDashboard resumen() {
        return new ResumenDashboard(
                usuarioRepository.count(),
                usuarioRepository.countByRol(Rol.CLIENTE),
                productoRepository.countByActivoTrue(),
                productoRepository.valorInventario(),
                boletaRepository.count(),
                boletaRepository.totalVentas(),
                citaRepository.count(),
                mensajeRepository.countByLeidoFalse());
    }
}
