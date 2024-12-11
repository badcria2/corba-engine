package corba.engine.rules;

import corba.engine.models.KafkaData;
import corba.engine.request.KafkaRequest;
import corba.engine.response.NetworkElement;
import corba.engine.models.Persona;
import corba.engine.models.Tags;
import corba.engine.services.GraphQLService;
import corba.engine.suscriptors.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        logger.info("Procesando KafkaData desde source: " + kafkaData.getTags().getSource());

        graphQLService.getAllAvailableGroups()
                .doOnError(error -> logger.severe("Error al consultar grupos disponibles: " + error.getMessage()))
                .doOnNext(response -> {
                    if (response != null && response.getData() != null && response.getData().getGetAllAvailableGroups() != null) {
                        List<String> groups = response.getData().getGetAllAvailableGroups();
                        if (!groups.isEmpty()) {
                            logger.info("Grupos obtenidos: " + groups);
                            executeGetAllNetworkElementsByGroup(groups.get(0), kafkaData);
                        } else {
                            logger.warning("No se encontraron grupos disponibles en la respuesta.");
                        }
                    } else {
                        logger.warning("Respuesta de GraphQL nula o sin datos.");
                    }
                })
                .doOnTerminate(() -> logger.info("Finalizó la consulta de grupos."))
                .subscribe();

        return kafkaData;
    }


    public void executeGetAllNetworkElementsByGroup(String group, KafkaData kafkaData) {
        graphQLService.getAllNetworkElementsByGroup(group)
                .doOnTerminate(() -> {
                    System.out.println("Consulta de GraphQL terminada.");
                })
                .subscribe(response -> {
                    if (response != null && response.getData() != null) {
                        System.out.println("Datos de la red obtenidos: " + response.getData().getAllNetworkElementsByGroup());

                        // Buscar el nombre asociado al source
                        String source = kafkaData.getTags().getSource();
                        String name = findNameByManagementIp(source, response.getData().getAllNetworkElementsByGroup());

                        // Crear la lista de mensajes para KafkaRequest
                        List<Map<String, Object>> messages = new ArrayList<>();
                        Map<String, Object> message = new HashMap<>();

                        if (name != null) {
                            message.put("source", source);
                            message.put("message", "El nombre asociado al source " + source + " es: " + name);
                        } else {
                            message.put("source", source);
                            message.put("message", "No se encontró un nombre asociado al source " + source);
                        }

                        messages.add(message);

                        // Crear KafkaRequest y enviarlo
                        KafkaRequest kafkaRequest = new KafkaRequest(messages);
                        kafkaProducerService.sendMessage("opt-alert-drools", kafkaRequest);

                        System.out.println("Mensaje enviado: " + kafkaRequest);
                    }
                });
    }

    // Método para buscar el nombre basado en la IP de gestión
    private String findNameByManagementIp(String source, List<NetworkElement> elements) {
        for (NetworkElement element : elements) {
            if (source.equals(element.getManagementIp())) {
                return element.getName();
            }
        }
        return null; // Si no se encuentra la IP, retorna null
    }

    @Autowired
    private KafkaProducerService kafkaProducerService;

    public void enviarDatos(KafkaRequest kafkaDataList) {
        String topic = "opt-alert-drools";  // Especifica el tópico en el que deseas enviar los datos
        kafkaProducerService.sendMessage(topic, kafkaDataList);
    }
}
