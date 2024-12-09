package corba.engine.rules;

import corba.engine.models.KafkaData;
import corba.engine.models.Persona;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.Decoder;
import org.springframework.stereotype.Service;


import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.Decoder;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

@Service
public class EventCorbaServiceImpl implements EventCorbaService {
    private static final Logger logger = Logger.getLogger(EventCorbaServiceImpl.class.getName());

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
    public KafkaData evalueAvailablesGroups(KafkaData kafkaData) {
        System.out.println("INGRESANDO A DROOLSS:: ");
        logger.info("Ingresando a adroos_" + kafkaData.getTags().getSource());
        return kafkaData;
    }


}
