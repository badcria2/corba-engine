package corba.engine.suscriptors;

import com.fasterxml.jackson.databind.ObjectMapper;
import corba.engine.models.KafkaData;
import corba.engine.request.KafkaRequest;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class KafkaProducerService {

    @Value("${kafka.bootstrap.servers:10.95.90.65:9091,10.95.90.65:9092}")
    private String bootstrapServers; // Dirección del broker Kafka

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendMessage(String topic, KafkaRequest kafkaDataList) {
        System.out.println("ACA ANDAMOS");
        // Verificar o crear el tópico
        ensureTopicExists(topic);

        // Configuración del productor Kafka
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Crear el productor Kafka
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        try {
            // Convertir la lista de datos a JSON
            String jsonMessage = objectMapper.writeValueAsString(kafkaDataList);

            // Crear un record para enviar al tópico
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, "key", jsonMessage);

            // Enviar el mensaje al tópico
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    exception.printStackTrace();
                    System.err.println("Error al enviar mensaje: " + exception.getMessage());
                } else {
                    System.out.println("Mensaje enviado exitosamente: " + metadata.toString());
                }
            });

        } catch (Exception e) {
            System.out.println("Error al enviar mensaje Kafka: " + e.getMessage());
        } finally {
            // Cerrar el productor
            producer.close();
        }
    }

    private void ensureTopicExists(String topic) {

        System.out.println("ACA ANDAMOS2");
        // Configuración del AdminClient
        Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        System.out.println("ACA ANDAMOS2");
        try (AdminClient adminClient = AdminClient.create(properties)) {
            // Obtener la lista de tópicos existentes
            Set<String> topics = adminClient.listTopics().names().get();

            if (!topics.contains(topic)) {
                // Crear el tópico si no existe
                NewTopic newTopic = new NewTopic(topic, 1, (short) 1); // 1 partición, factor de replicación 1
                adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
                System.out.println("Tópico creado: " + topic);
            } else {
                System.out.println("El tópico ya existe: " + topic);
            }
        } catch (ExecutionException | InterruptedException e) {
            System.err.println("Error al verificar o crear el tópico: " + e.getMessage());
        }
    }
}
