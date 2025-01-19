package corba.engine.rules;

import corba.engine.models.KafkaData;
import corba.engine.models.Persona;

public interface EventCorbaService {
    Persona enviarcampania(Persona persona);
    Persona enviarcampaniaMenor(Persona persona);

    KafkaData evalueAvailablesGroups(KafkaData kafkaData);

    void sendMessage(String neName, String hostname, String interfaces, String description);


}