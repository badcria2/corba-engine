package corba.engine.dao.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "Enlace_Servicio")
public class EnlaceServicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "enlace_id")
    private Enlace enlace;

    @ManyToOne
    @JoinColumn(name = "servicio_id")
    private Servicio servicio;

    // Getters y Setters
}