package corba.engine.rules;

import corba.engine.dao.entity.Puerto;
import corba.engine.dao.response.ClienteInfoDTO;
import corba.engine.models.Persona;

import java.util.List;

public interface PersonService {
    Persona enviarcampania(Persona persona);
    List<Puerto> validarNE(List<Puerto> puertos);
}