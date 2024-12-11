package corba.engine.suscriptors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import corba.engine.AvroDeserializer;
import corba.engine.models.KafkaData;
import corba.engine.models.Tags;
import corba.engine.services.RuleService;
import org.apache.avro.AvroRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;

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
    @KafkaListener(topics = "opt-term-inout-pwr", groupId = "mi-grupo-consumidor")
    public void listen_opt_term_inout_pwr(String message) {

        try {
            // Preprocesar el JSON antes de deserializarlo
            String avroJsonDeserializeTemp = preprocessJson(avroJsonDeserialize);

            // Deserializar el JSON
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

            List<KafkaData> kafkaDataList = objectMapper.readValue(avroJsonDeserializeTemp, objectMapper.getTypeFactory().constructCollectionType(List.class, KafkaData.class));

            // Imprimir el resultado
            for (KafkaData data : kafkaDataList) {
                System.out.println(data);
            }
            processKafkaData(kafkaDataList);
        } catch (JsonProcessingException e) {
            System.out.println("Error de procesamiento JSON: " + e);
        }       catch (AvroRuntimeException e) {
            System.out.println("Error al deserializar AVRO: " +  e);
        } catch (Exception e) {
            System.out.println("Error general procesando el mensaje: " + e);
        }
    }
    @KafkaListener(topics = "opt-term-target-out-pwr", groupId = "mi-grupo-consumidor")
    public void listen_opt_term_target_out_pwr(String message) {
        try {
            // Preprocesar el JSON antes de deserializarlo
            String avroJsonDeserializeTemp = preprocessJson(avroJsonDeserialize);

            // Deserializar el JSON
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

            List<KafkaData> kafkaDataList = objectMapper.readValue(avroJsonDeserializeTemp, objectMapper.getTypeFactory().constructCollectionType(List.class, KafkaData.class));

            // Imprimir el resultado
            for (KafkaData data : kafkaDataList) {
                System.out.println(data);
            }
            processKafkaData(kafkaDataList);
        } catch (JsonProcessingException e) {
            System.out.println("Error de procesamiento JSON: " + e);
        }       catch (AvroRuntimeException e) {
            System.out.println("Error al deserializar AVRO: " +  e);
        } catch (Exception e) {
            System.out.println("Error general procesando el mensaje: " + e);
        }
    }
    private static String preprocessJson(String json) throws Exception {
        // Crear un ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();

        // Convertir el JSON a un JsonNode
        JsonNode rootNode = objectMapper.readTree(json);

        // Iterar sobre los elementos y reemplazar los campos con guiones
        if (rootNode.isArray()) {
            for (JsonNode element : rootNode) {
                // Verificar si el campo "tags" existe y es un objeto
                JsonNode tagsNode = element.get("tags");
                if (tagsNode != null && tagsNode.isObject()) {
                    // Cambiar "subscription-name" a "subscription_name"
                    replaceFieldName((ObjectNode) tagsNode, "subscription-name", "subscription_name");
                }
            }
        }

        // Convertir de nuevo el JsonNode a una cadena JSON
        return objectMapper.writeValueAsString(rootNode);
    }

    // Método para reemplazar un nombre de campo en un ObjectNode
    private static void replaceFieldName(ObjectNode objectNode, String oldName, String newName) {
        Iterator<String> fieldNames = objectNode.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            if (fieldName.equals(oldName)) {
                JsonNode fieldValue = objectNode.remove(fieldName);  // Eliminar el campo original
                objectNode.set(newName, fieldValue);  // Establecer el campo con el nuevo nombre
            }
        }
    }
    private void processKafkaData(List<KafkaData> data) {
        for (KafkaData dataEvaluar: data) {
            Tags tags = dataEvaluar.getTags();
            ruleService.executeRulesWithEventKafka(dataEvaluar);
        }


    }
}
