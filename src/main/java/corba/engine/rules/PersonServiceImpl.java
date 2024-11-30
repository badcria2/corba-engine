package corba.engine.rules;

import corba.engine.models.Persona;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class PersonServiceImpl implements PersonService {
    private static final Logger logger = Logger.getLogger(PersonServiceImpl.class.getName());

    @Override
    public Persona enviarcampania(Persona persona) {

        logger.info("Enviando campaña para persona: " + persona.getNombre() + ", edad: " + persona.getEdad());
        persona.setCampania("Campaña para mayores de edad");
        return persona;
    }
    public Persona enviarcampaniaMenor(Persona persona) {

        logger.info("Enviando campaña para persona: " + persona.getNombre() + ", edad: " + persona.getEdad());
        persona.setCampania("Campaña para menor de edad");
        return persona;
    }


}
