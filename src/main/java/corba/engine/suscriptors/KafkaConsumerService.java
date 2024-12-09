package corba.engine.suscriptors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import corba.engine.AvroDeserializer;
import corba.engine.models.KafkaData;
import corba.engine.models.Tags;
import corba.engine.services.RuleService;
import org.apache.avro.AvroRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KafkaConsumerService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RuleService ruleService;

    @Autowired
    public KafkaConsumerService(RuleService ruleService) {
        this.ruleService = ruleService;
    }


    @KafkaListener(topics = "__consumer_offsets", groupId = "mi-grupo-consumidor")
    public void listen(String message) {
        try {
            String schemaString = "{\n" +
                    "  \"type\": \"record\",\n" +
                    "  \"name\": \"KafkaData\",\n" +
                    "  \"fields\": [\n" +
                    "    {\"name\": \"name\", \"type\": \"string\"},\n" +
                    "    {\"name\": \"timestamp\", \"type\": \"long\"},\n" +
                    "    {\"name\": \"tags\", \"type\": {\n" +
                    "      \"type\": \"record\",\n" +
                    "      \"name\": \"Tags\",\n" +
                    "      \"fields\": [\n" +
                    "        {\"name\": \"component_name\", \"type\": \"string\"},\n" +
                    "        {\"name\": \"source\", \"type\": \"string\"},\n" +
                    "        {\"name\": \"subscription-name\", \"type\": \"string\"}\n" +
                    "      ]\n" +
                    "    }},\n" +
                    "    {\"name\": \"values\", \"type\": {\n" +
                    "      \"type\": \"map\",\n" +
                    "      \"values\": \"string\"\n" +
                    "    }}\n" +
                    "  ]\n" +
                    "}";

            // Luego puedes usar `schemaString` en el método de deserialización
            byte[] avroData = message.getBytes("UTF-8");  // Convierte el mensaje a bytes si es necesario
            List<KafkaData> kafkaDataList = AvroDeserializer.deserializeAvroList(avroData, schemaString);


            processKafkaData(kafkaDataList);
        } catch (JsonProcessingException e) {
            System.out.println("Error de procesamiento JSON: " + e);
        }       catch (AvroRuntimeException e) {
            System.out.println("Error al deserializar AVRO: " +  e);
        } catch (Exception e) {
            System.out.println("Error general procesando el mensaje: " + e);
        }
    }
    private void processKafkaData(List<KafkaData> data) {
        for (KafkaData dataEvaluar: data) {
            Tags tags = dataEvaluar.getTags();
            if ("10.95.90.87".equals(tags.getSource())) {
                System.out.println("Alerta: Los datos provienen de la fuente especificada (" + tags.getSource() + ").");
            }
            ruleService.executeRulesWithEventKafka(dataEvaluar);

            System.out.println("Datos transformados: ");
            System.out.println("Nombre: " + dataEvaluar.getName());
            System.out.println("Timestamp: " + dataEvaluar.getTimestamp());
            System.out.println("Componente: " + tags.getComponentName());
            System.out.println("Source: " + tags.getSource());
            System.out.println("Subscription Name: " + tags.getSubscriptionName());
            System.out.println("Values: " + dataEvaluar.getValues());
        }


    }
}
