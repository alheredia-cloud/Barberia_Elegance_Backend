package cl.duoc.barberia.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categoria")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    /**
     * true  = categoria de servicios (cortes, barba, combos): stock ilimitado y requiere agendar cita.
     * false = productos fisicos con control de stock numerico.
     */
    @Builder.Default
    @Column(name = "es_servicio", nullable = false)
    private boolean esServicio = false;
}
