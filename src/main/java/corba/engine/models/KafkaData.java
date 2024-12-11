package corba.engine.models;

import java.util.Map;


// Getters y setters
public class KafkaData {
    private String name;

    private long timestamp;
    private Tags tags;
    private Map<String, String> values;

    // Getters y setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Tags getTags() {
        return tags;
    }

    public void setTags(Tags tags) {
        this.tags = tags;
    }

    public Map<String, String> getValues() {
        return values;
    }

    public void setValues(Map<String, String> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "KafkaData{" +
                "name='" + name + '\'' +
                ", timestamp=" + timestamp +
                ", tags=" + tags +
                ", values=" + values +
                '}';
    }
}