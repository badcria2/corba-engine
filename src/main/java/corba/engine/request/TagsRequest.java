package corba.engine.request;

public class TagsRequest {
    private String source;
    private String ruleName;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    @Override
    public String toString() {
        return "Tags{" +
                "source='" + source + '\'' +
                ", ruleName='" + ruleName + '\'' +
                '}';
    }
}
