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

public class KafkaProducerService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public static void sendMessageToKafka(String topic, String message) {
        // Configuración del productor de Kafka
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // Dirección de tu servidor Kafka
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Crear el productor
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(properties)) {
            // Crear el registro que se enviará
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, message);

            // Enviar el mensaje y manejar la respuesta
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    System.err.println("Error al enviar mensaje: " + exception.getMessage());
                } else {
                    System.out.println("Mensaje enviado con éxito. Offset: " + metadata.offset());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
