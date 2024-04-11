package corba.engine.rules;

import corba.engine.dao.entity.NE;
import corba.engine.dao.entity.Puerto;
import corba.engine.dao.response.ClienteInfoDTO;
import corba.engine.models.Persona;
import corba.engine.services.RuleService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    @Override
    public List<Puerto> validarNE(List<Puerto> puertos) {
        for (Puerto puerto : puertos) {
            NE newNE = new NE();
            newNE.setActivo(true);
            newNE.setId(puerto.getNe().getId());
            newNE.setNombre(puerto.getNe().getNombre());

            puerto.setNe(newNE);
        }
        return puertos;
    }

}
