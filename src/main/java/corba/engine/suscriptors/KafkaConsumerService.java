package corba.engine.suscriptors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.node.ObjectNode;
import corba.engine.models.KafkaData;
import corba.engine.services.RuleService;
import org.apache.avro.AvroRuntimeException;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class KafkaConsumerService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RuleService ruleService;
    private String avroJsonDeserialize;

    @Autowired
    public KafkaConsumerService(RuleService ruleService) {
        this.ruleService = ruleService;
        this.avroJsonDeserialize = "[ { \"name\" : \"default-1733336542\", \"timestamp\" : 1733347504252893801, \"tags\" : { \"component_name\" : \"och 1/2/c1\", \"source\" : \"10.95.90.87\", \"subscription-name\" : \"default-1733336542\" }, \"values\" : { \"/components/component/optical-channel/state/output-power/instant\" : \"-8.32\" } } ]";
    }

    @KafkaListener(topics = "opt-term-target-out-pwr", groupId = "mi-grupo-consumidor")
    public void listen_opt_term_target_out_pwr(String message, Consumer<?, ?> consumer) {
        try {
            // Posicionar el consumidor en los últimos mensajes disponibles
            String topic = "opt-term-target-out-pwr";
            List<TopicPartition> partitions = consumer.partitionsFor(topic).stream()
                    .map(partitionInfo -> new TopicPartition(partitionInfo.topic(), partitionInfo.partition()))
                    .collect(Collectors.toList());

            consumer.assign(partitions);
            consumer.seekToEnd(partitions);

            // Mostrar los últimos offsets en cada partición
            for (TopicPartition partition : partitions) {
                long offset = consumer.position(partition);
                System.out.println("Iniciando desde el último offset en la partición " + partition.partition() + ": " + offset);
            }

            // Preprocesar el JSON antes de deserializarlo
            String avroJsonDeserializeTemp = preprocessJson(message);

            // Deserializar el JSON
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            List<KafkaData> kafkaDataList = objectMapper.readValue(avroJsonDeserializeTemp, objectMapper.getTypeFactory().constructCollectionType(List.class, KafkaData.class));

            // Procesar los datos de Kafka
            processKafkaData(kafkaDataList);

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

    private void processKafkaData(List<KafkaData> data) {
        for (KafkaData dataEvaluar : data) {
            String uniqueId = dataEvaluar.getTimestamp() + "-" + dataEvaluar.getTags().getComponentName();

            System.out.println("Data a evaluar: " + dataEvaluar.toString());
            ruleService.executeRulesWithEventKafka(dataEvaluar);
        }
    }
}
