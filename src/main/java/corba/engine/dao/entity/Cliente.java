package corba.engine.dao.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "cliente")
@Data
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String dni;
    private String nombre;
    private String apellido;
    private String direccion;

    // Getters y Setters
}