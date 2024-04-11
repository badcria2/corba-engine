package corba.engine.dao.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "Servicio")
public class Servicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion = "Servicio recientemente contratado";
    private Boolean habilitado = false;
}