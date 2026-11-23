package cl.duoc.barberia.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

/** Reserva de hora asociada a la compra de servicios. */
@Entity
@Table(name = "cita", uniqueConstraints = @UniqueConstraint(name = "uk_cita_fecha_hora", columnNames = {"fecha", "hora"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boleta_id")
    private Boleta boleta;

    @Column(name = "nombre_cliente", nullable = false, length = 100)
    private String nombreCliente;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    /** Nombres de los servicios separados por coma. */
    @Column(nullable = false, length = 500)
    private String servicios;

    @Column(length = 500)
    private String notas;
}
