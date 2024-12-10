package corba.engine.rules;

import corba.engine.models.KafkaData;
import corba.engine.response.NetworkElement;
import corba.engine.models.Persona;
import corba.engine.models.Tags;
import corba.engine.services.GraphQLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
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
                    // Después de que la consulta termine
                    System.out.println("Consulta de GraphQL terminada.");
                })
                .subscribe(response -> {
                    // Procesar la respuesta
                    if (response != null && response.getData() != null) {
                        System.out.println("Grupos disponibles: " + response.getData().getGetAllAvailableGroups());

                        // Llamar al siguiente método con un grupo específico
                        executeGetAllNetworkElementsByGroup(response.getData().getGetAllAvailableGroups().get(1), kafkaData);
                    }
                });

        return kafkaData;
    }

    private void executeGetAllNetworkElementsByGroup(String group, KafkaData kafkaData) {
        graphQLService.getAllNetworkElementsByGroup(group)
                .doOnTerminate(() -> {
                    // Después de que la consulta termine
                    System.out.println("Consulta de GraphQL terminada.");
                })
                .subscribe(response -> {
                    // Procesar la respuesta
                    if (response != null && response.getData() != null) {
                        System.out.println("Datos de la red obtenidos: " + response.getData().getAllNetworkElementsByGroup());

                        // Buscar el elemento asociado al source de KafkaData
                        String source = kafkaData.getTags().getSource(); // Obtener el source de KafkaData
                        String name = findNameByManagementIp(source, response.getData().getAllNetworkElementsByGroup());

                        if (name != null) {
                            System.out.println("El nombre asociado al source " + source + " es: " + name);
                        } else {
                            System.out.println("No se encontró un nombre asociado al source " + source);
                        }
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



}
