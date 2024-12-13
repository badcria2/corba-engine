package corba.engine.request;

import java.util.List;
import java.util.Map;

public class KafkaRequest {
    private String name;
    private long timestamp;
    private TagsRequest tags;
    private List<Map<String, Object>> values;

    // Getters y Setters
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

    public TagsRequest getTags() {
        return tags;
    }

    public void setTags(TagsRequest tags) {
        this.tags = tags;
    }

    public List<Map<String, Object>> getValues() {
        return values;
    }

    public void setValues(List<Map<String, Object>> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "KafkaRequest{" +
                "name='" + name + '\'' +
                ", timestamp=" + timestamp +
                ", tags=" + tags +
                ", values=" + values +
                '}';
    }
}