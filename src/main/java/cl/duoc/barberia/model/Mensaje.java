package cl.duoc.barberia.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Mensaje enviado desde el formulario de contacto. */
@Entity
@Table(name = "mensaje")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(nullable = false, length = 1000)
    private String contenido;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private boolean leido = false;
}
