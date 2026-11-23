package cl.duoc.barberia.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "producto")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    /** Precio en pesos chilenos (CLP). */
    @Column(nullable = false)
    private Integer precio;

    /** Stock numerico. Es null cuando el producto es un servicio (stock ilimitado). */
    private Integer stock;

    @Column(length = 255)
    private String imagen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    /** Borrado logico: los productos con ventas no se eliminan fisicamente. */
    @Builder.Default
    @Column(nullable = false)
    private boolean activo = true;

    public boolean isServicio() {
        return categoria != null && categoria.isEsServicio();
    }
}
