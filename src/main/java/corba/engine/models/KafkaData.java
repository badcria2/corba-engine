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
    public Double getTargetOutputPower() {
        String valueStr = values.get("/components/component/optical-channel/state/target-output-power");
        if (valueStr != null) {
            try {
                return Double.parseDouble(valueStr);
            } catch (NumberFormatException e) {
                return null; // Devuelve null si no se puede convertir
            }
        }
        return null; // Devuelve null si no existe el valor
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