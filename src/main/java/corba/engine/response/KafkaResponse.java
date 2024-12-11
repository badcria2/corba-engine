package corba.engine.response;

import java.util.List;
import java.util.Map;

public class KafkaResponse {
    private String topic;
    private List<Map<String, Object>> messages;

    public KafkaResponse() {
    }

    public KafkaResponse(String topic, List<Map<String, Object>> messages) {
        this.topic = topic;
        this.messages = messages;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<Map<String, Object>> getMessages() {
        return messages;
    }

    public void setMessages(List<Map<String, Object>> messages) {
        this.messages = messages;
    }

    @Override
    public String toString() {
        return "KafkaResponse{" +
                "topic='" + topic + '\'' +
                ", messages=" + messages +
                '}';
    }
}