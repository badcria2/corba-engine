package corba.engine.suscriptors;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "__consumer_offsets", groupId = "mi-grupo-consumidor")
    public void listen(String message) {
        System.out.println("Mensaje recibido: " + message);
    }
}
