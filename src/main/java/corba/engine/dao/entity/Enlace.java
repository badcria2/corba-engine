package corba.engine.dao.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "Enlace")
public class Enlace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;
    // Otros atributos según tus necesidades

    // Getters y Setters
}