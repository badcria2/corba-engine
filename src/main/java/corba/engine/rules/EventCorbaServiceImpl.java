package corba.engine.rules;

import corba.engine.models.KafkaData;
import corba.engine.models.Persona;
import corba.engine.models.Tags;
import corba.engine.services.GraphQLService;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.Decoder;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final GraphQLService graphQLService;

    private static final Logger logger = Logger.getLogger(EventCorbaServiceImpl.class.getName());
    @Autowired
    public EventCorbaServiceImpl(GraphQLService graphQLService) {
        this.graphQLService = graphQLService;
    }
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
        // Llamada a getAllAvailableGroups
        graphQLService.getAllAvailableGroups()
                .doOnTerminate(() -> {
                    // Después de que la consulta termine, puedes procesar los grupos obtenidos
                    System.out.println("Consulta de GraphQL terminada.");
                })
                .subscribe(response -> {
                    // Aquí puedes procesar los grupos disponibles de la respuesta
                    if (response != null && response.getData() != null) {
                        System.out.println("Grupos disponibles: " + response.getData().getGetAllAvailableGroups());
                        // Puedes tomar decisiones basadas en los grupos obtenidos
                    }
                });

        return kafkaData;
    }


}
