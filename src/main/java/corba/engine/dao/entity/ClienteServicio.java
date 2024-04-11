package corba.engine.dao.entity;
import lombok.Data;

import javax.persistence.*;
@Data
@Entity
@Table(name = "cliente_servicio")
public class ClienteServicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_servicio")
    private Servicio servicio;
    // Otros atributos y métodos getters/setters
}