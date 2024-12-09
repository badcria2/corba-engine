package corba.engine.suscriptors;

import com.fasterxml.jackson.databind.ObjectMapper;
import corba.engine.models.KafkaData;
import corba.engine.models.Tags;
import corba.engine.services.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

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
            KafkaData kafkaData = objectMapper.readValue(message, KafkaData.class);
            processKafkaData(kafkaData);
        } catch (Exception e) {
            System.err.println("Error procesando el mensaje: " + e.getMessage());
        }
    }
    private void processKafkaData(KafkaData data) {
        Tags tags = data.getTags();
        if ("10.95.90.87".equals(tags.getSource())) {
            System.out.println("Alerta: Los datos provienen de la fuente especificada (" + tags.getSource() + ").");
        }
        ruleService.executeRulesWithEventKafka(data);

        System.out.println("Datos transformados: ");
        System.out.println("Nombre: " + data.getName());
        System.out.println("Timestamp: " + data.getTimestamp());
        System.out.println("Componente: " + tags.getComponentName());
        System.out.println("Source: " + tags.getSource());
        System.out.println("Subscription Name: " + tags.getSubscriptionName());
        System.out.println("Values: " + data.getValues());
    }
}
