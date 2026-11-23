package cl.duoc.barberia.service;

import cl.duoc.barberia.dto.AgendaDto.CitaRequest;
import cl.duoc.barberia.dto.AgendaDto.CitaResponse;
import cl.duoc.barberia.exception.BusinessException;
import cl.duoc.barberia.exception.ResourceNotFoundException;
import cl.duoc.barberia.model.Boleta;
import cl.duoc.barberia.model.Cita;
import cl.duoc.barberia.model.Usuario;
import cl.duoc.barberia.repository.BoletaRepository;
import cl.duoc.barberia.repository.CitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CitaService {

    /** Horario de atencion de la barberia. */
    static final LocalTime APERTURA = LocalTime.of(9, 0);
    static final LocalTime CIERRE = LocalTime.of(20, 0);

    private final CitaRepository citaRepository;
    private final BoletaRepository boletaRepository;
    private final UsuarioService usuarioService;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<CitaResponse> listar(LocalDate fecha) {
        List<Cita> citas = fecha == null
                ? citaRepository.findAllByOrderByFechaAscHoraAsc()
                : citaRepository.findByFechaOrderByHoraAsc(fecha);
        return citas.stream().map(CitaService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CitaResponse> listarPorUsuario(String email) {
        return citaRepository.findByUsuarioEmailIgnoreCaseOrderByFechaAscHoraAsc(email)
                .stream().map(CitaService::toResponse).toList();
    }

    /** Horas ya tomadas en un dia (para deshabilitarlas en el calendario del frontend). */
    @Transactional(readOnly = true)
    public List<LocalTime> horasOcupadas(LocalDate fecha) {
        return citaRepository.findByFechaOrderByHoraAsc(fecha).stream().map(Cita::getHora).toList();
    }

    @Transactional(readOnly = true)
    public CitaResponse obtener(Long id) {
        return toResponse(buscar(id));
    }

    @Transactional
    public CitaResponse crear(CitaRequest req, String emailUsuario) {
        validarHorario(req.fecha(), req.hora());
        if (citaRepository.existsByFechaAndHora(req.fecha(), req.hora())) {
            throw new BusinessException("La hora seleccionada ya esta reservada", HttpStatus.CONFLICT);
        }
        Usuario usuario = usuarioService.buscarPorEmail(emailUsuario);
        Cita cita = Cita.builder()
                .usuario(usuario)
                .nombreCliente(req.nombreCliente().trim())
                .fecha(req.fecha())
                .hora(req.hora())
                .servicios(String.join(", ", req.servicios()))
                .notas(req.notas())
                .boleta(resolverBoleta(req.boletaId(), emailUsuario))
                .build();
        return toResponse(citaRepository.save(cita));
    }

    @Transactional
    public CitaResponse actualizar(Long id, CitaRequest req) {
        Cita cita = buscar(id);
        validarHorario(req.fecha(), req.hora());
        if (citaRepository.existsByFechaAndHoraAndIdNot(req.fecha(), req.hora(), id)) {
            throw new BusinessException("La hora seleccionada ya esta reservada", HttpStatus.CONFLICT);
        }
        cita.setNombreCliente(req.nombreCliente().trim());
        cita.setFecha(req.fecha());
        cita.setHora(req.hora());
        cita.setServicios(String.join(", ", req.servicios()));
        cita.setNotas(req.notas());
        return toResponse(cita);
    }

    @Transactional
    public void eliminar(Long id) {
        citaRepository.delete(buscar(id));
    }

    /** Reglas: no se agenda en el pasado, solo dentro del horario y en bloques de 30 minutos. */
    void validarHorario(LocalDate fecha, LocalTime hora) {
        LocalDate hoy = LocalDate.now(clock);
        if (fecha.isBefore(hoy) || (fecha.isEqual(hoy) && !hora.isAfter(LocalTime.now(clock)))) {
            throw new BusinessException("No se puede agendar una cita en el pasado");
        }
        if (hora.isBefore(APERTURA) || !hora.isBefore(CIERRE)) {
            throw new BusinessException("El horario de atencion es de 09:00 a 20:00");
        }
        if (hora.getMinute() % 30 != 0 || hora.getSecond() != 0) {
            throw new BusinessException("Las citas se agendan en bloques de 30 minutos");
        }
    }

    private Boleta resolverBoleta(Long boletaId, String email) {
        if (boletaId == null) {
            return null;
        }
        Boleta b = boletaRepository.findById(boletaId)
                .orElseThrow(() -> new ResourceNotFoundException("Boleta", boletaId));
        if (!b.getUsuario().getEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("La boleta no pertenece al usuario");
        }
        return b;
    }

    private Cita buscar(Long id) {
        return citaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cita", id));
    }

    static CitaResponse toResponse(Cita c) {
        List<String> servicios = Arrays.stream(c.getServicios().split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
        return new CitaResponse(c.getId(), c.getNombreCliente(),
                c.getUsuario() != null ? c.getUsuario().getEmail() : null,
                c.getFecha(), c.getHora(), servicios, c.getNotas(),
                c.getBoleta() != null ? c.getBoleta().getId() : null);
    }
}
