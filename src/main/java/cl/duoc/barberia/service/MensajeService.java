package cl.duoc.barberia.service;

import cl.duoc.barberia.dto.AgendaDto.MensajeRequest;
import cl.duoc.barberia.dto.AgendaDto.MensajeResponse;
import cl.duoc.barberia.exception.ResourceNotFoundException;
import cl.duoc.barberia.model.Mensaje;
import cl.duoc.barberia.repository.MensajeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MensajeService {

    private final MensajeRepository mensajeRepository;

    @Transactional(readOnly = true)
    public List<MensajeResponse> listar() {
        return mensajeRepository.findAllByOrderByFechaDesc().stream().map(MensajeService::toResponse).toList();
    }

    @Transactional
    public MensajeResponse crear(MensajeRequest req) {
        Mensaje m = Mensaje.builder()
                .nombre(req.nombre().trim())
                .email(req.email().trim().toLowerCase())
                .contenido(req.mensaje().trim())
                .build();
        return toResponse(mensajeRepository.save(m));
    }

    @Transactional
    public MensajeResponse marcarLeido(Long id, boolean leido) {
        Mensaje m = buscar(id);
        m.setLeido(leido);
        return toResponse(m);
    }

    @Transactional
    public void eliminar(Long id) {
        mensajeRepository.delete(buscar(id));
    }

    @Transactional
    public void eliminarTodos() {
        mensajeRepository.deleteAllInBatch();
    }

    private Mensaje buscar(Long id) {
        return mensajeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Mensaje", id));
    }

    static MensajeResponse toResponse(Mensaje m) {
        return new MensajeResponse(m.getId(), m.getNombre(), m.getEmail(), m.getContenido(), m.getFecha(), m.isLeido());
    }
}
