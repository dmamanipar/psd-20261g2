package pe.edu.upeu.sysalmacen.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "upeu_vent_carrito")
public class VentCarrito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carrito")
    private Long idCarrito;

    @Column(name = "dniruc", nullable = false, length = 12)
    private String dniruc;

    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(name = "nombre_producto", nullable = false, length = 120)
    private String nombreProducto;

    @Column(name = "cantidad", nullable = false)
    private Double cantidad;

    @Column(name = "punitario", nullable = false)
    private Double punitario;

    @Column(name = "ptotal", nullable = false)
    private Double ptotal;

    @Column(name = "estado", nullable = false)
    private int estado;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
}

