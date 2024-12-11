package corba.engine.request;

import java.util.List;
import java.util.Map;

public class KafkaRequest {
    private List<Map<String, Object>> messages;

    public KafkaRequest() {
    }

    public KafkaRequest(List<Map<String, Object>> messages) {
        this.messages = messages;
    }

    public List<Map<String, Object>> getMessages() {
        return messages;
    }

    public void setMessages(List<Map<String, Object>> messages) {
        this.messages = messages;
    }

    @Override
    public String toString() {
        return "KafkaRequest{" +
                "messages=" + messages +
                '}';
    }
}