package corba.engine.rules;

import corba.engine.models.KafkaData;
import corba.engine.request.KafkaRequest;
import corba.engine.response.NetworkElement;
import corba.engine.models.Persona;
import corba.engine.services.GraphQLService;
import corba.engine.suscriptors.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EventCorbaServiceImpl implements EventCorbaService {

    private final GraphQLService graphQLService;
    private final KafkaProducerService kafkaProducerService;

    @Autowired
    public EventCorbaServiceImpl(GraphQLService graphQLService, KafkaProducerService kafkaProducerService) {
        this.graphQLService = graphQLService;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Override
    public Persona enviarcampania(Persona persona) {
        System.out.println("Enviando campaña para persona: " + persona.getNombre() + ", edad: " + persona.getEdad());
        persona.setCampania("Campaña para mayores de edad");
        return persona;
    }

    public Persona enviarcampaniaMenor(Persona persona) {
        System.out.println("Enviando campaña para persona: " + persona.getNombre() + ", edad: " + persona.getEdad());
        persona.setCampania("Campaña para menor de edad");
        return persona;
    }

    public KafkaData evalueAvailablesGroups(KafkaData kafkaData) {
        System.out.println("Procesando grupos disponibles para KafkaData: " + kafkaData.getTags().getSource());

        graphQLService.getAllAvailableGroups()
                .doOnTerminate(() -> System.out.println("Consulta de grupos disponibles finalizada."))
                .subscribe(response -> {
                    if (response != null && response.getData() != null) {
                        List<String> availableGroups = response.getData().getGetAllAvailableGroups();
                        System.out.println("Grupos disponibles obtenidos: " + availableGroups);

                        if (!availableGroups.isEmpty()) {
                            executeGetAllNetworkElementsByGroup(availableGroups.get(1), kafkaData);
                        } else {
                            System.out.println("No se encontraron grupos disponibles.");
                        }
                    }
                }, error -> System.out.println("Error al obtener grupos disponibles: " + error));

        return kafkaData;
    }

    private void executeGetAllNetworkElementsByGroup(String group, KafkaData kafkaData) {
        System.out.println("Procesando elementos de red para el grupo: " + group);

        graphQLService.getAllNetworkElementsByGroup(group)
                .doOnTerminate(() ->
                        System.out.println("Consulta de elementos de red finalizada para el grupo: " + group) )
                .subscribe(response -> {
                    if (response != null && response.getData() != null) {
                        List<NetworkElement> networkElements = response.getData().getAllNetworkElementsByGroup();
                        System.out.println("Elementos de red obtenidos: " + networkElements);

                        processAndSendToKafka(kafkaData, networkElements);
                    } else {
                        System.out.println("No se obtuvieron datos de la red para el grupo: " + group);
                    }
                }, error -> System.out.println("Error al obtener elementos de red para el grupo: " + group + " - " + error));
    }

    private void processAndSendToKafka(KafkaData kafkaData, List<NetworkElement> networkElements) {
        String source = kafkaData.getTags().getSource();
        String name = findNameByManagementIp(source, networkElements);

        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> message = new HashMap<>();

        if (name != null) {
            message.put("source", source);
            message.put("message", "El nombre asociado al source " + source + " es: " + name);
            System.out.println("Nombre encontrado para el source " + source + ": " + name);
        } else {
            message.put("source", source);
            message.put("message", "No se encontró un nombre asociado al source " + source);
            System.out.println("No se encontró un nombre asociado al source: " + source);
        }

        messages.add(message);
        sendMessageToKafka(new KafkaRequest(messages));
    }

    private void sendMessageToKafka(KafkaRequest kafkaRequest) {
        try {
            kafkaProducerService.sendMessage("opt-alert-drools", kafkaRequest);
            System.out.println("Mensaje enviado a Kafka: " + kafkaRequest);
        } catch (Exception e) {
            System.out.println("Error al enviar mensaje a Kafka: " + e);
        }
    }

    private String findNameByManagementIp(String source, List<NetworkElement> elements) {
        for (NetworkElement element : elements) {
            if (source.equals(element.getManagementIp())) {
                return element.getName();
            }
        }
        return null;
    }
}
