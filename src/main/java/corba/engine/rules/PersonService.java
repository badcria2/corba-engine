package corba.engine.rules;

import corba.engine.models.Persona;

import java.util.List;

public interface PersonService {
    Persona enviarcampania(Persona persona);
    Persona enviarcampaniaMenor(Persona persona); 
}