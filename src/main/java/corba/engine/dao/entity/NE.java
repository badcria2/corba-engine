package corba.engine.dao.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "ne")
public class NE {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private boolean activo;

    // Getters y Setters
}