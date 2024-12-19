package corba.engine.suscriptors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.node.ObjectNode;
import corba.engine.models.KafkaData;
import corba.engine.services.RuleService;
import org.apache.avro.AvroRuntimeException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class KafkaConsumerService implements ConsumerSeekAware {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private final RuleService ruleService;

    private final ConcurrentHashMap<String, Long> processedMessages = new ConcurrentHashMap<>();
    private static final long TTL = TimeUnit.MINUTES.toMillis(5); // Tiempo para limpiar mensajes antiguos

    @Autowired
    public KafkaConsumerService(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @KafkaListener(topics = "opt-term-target-out-pwr", groupId = "mi-grupo-consumidor")
    public void listen_opt_term_target_out_pwr(ConsumerRecord<String, String> record) {
        try {
            // Procesar solo el último mensaje recibido
            System.out.println("Procesando último mensaje del tópico: " + record.value());

            String avroJsonDeserializeTemp = preprocessJson(record.value());
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

            List<KafkaData> kafkaDataList = objectMapper.readValue(
                    avroJsonDeserializeTemp,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, KafkaData.class)
            );

            for (KafkaData kafkaData : kafkaDataList) {
                System.out.println("Procesando data: " + kafkaData);
                ruleService.executeRulesWithEventKafka(kafkaData);
            }

            // Limpiar mensajes antiguos del mapa
            cleanupOldMessages();

        } catch (JsonProcessingException e) {
            System.out.println("Error de procesamiento JSON: " + e);
        } catch (AvroRuntimeException e) {
            System.out.println("Error al deserializar AVRO: " + e);
        } catch (Exception e) {
            System.out.println("Error general procesando el mensaje: " + e);
        }
    }

    private static String preprocessJson(String json) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(json);

        if (rootNode.isArray()) {
            for (JsonNode element : rootNode) {
                JsonNode tagsNode = element.get("tags");
                if (tagsNode != null && tagsNode.isObject()) {
                    replaceFieldName((ObjectNode) tagsNode, "subscription-name", "subscription_name");
                }
            }
        }
        return objectMapper.writeValueAsString(rootNode);
    }

    private static void replaceFieldName(ObjectNode objectNode, String oldName, String newName) {
        Iterator<String> fieldNames = objectNode.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            if (fieldName.equals(oldName)) {
                JsonNode fieldValue = objectNode.remove(fieldName);
                objectNode.set(newName, fieldValue);
            }
        }
    }

    private void cleanupOldMessages() {
        long currentTime = System.currentTimeMillis();
        processedMessages.entrySet().removeIf(entry -> currentTime - entry.getValue() > TTL);
    }

    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        // Reposicionar al último mensaje en cada partición asignada
        assignments.forEach((partition, offset) -> {
            System.out.println("Reposicionando al último mensaje del tópico: " + partition.topic());
            callback.seekToEnd(Collections.singleton(partition));
        });
    }
}
