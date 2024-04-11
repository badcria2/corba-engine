package corba.engine.dao.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "servicio_puerto")
public class ServicioPuerto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "puerto_id")
    private Puerto puerto;

    @ManyToOne
    @JoinColumn(name = "servicio_id")
    private Servicio servicio;
}