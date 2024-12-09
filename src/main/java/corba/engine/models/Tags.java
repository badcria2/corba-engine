package corba.engine.models;


import com.fasterxml.jackson.annotation.JsonProperty;

public class Tags {

        @JsonProperty("component_name")
        private String componentName;

        @JsonProperty("source")
        private String source;

        @JsonProperty("subscription_name")
        private String subscriptionName;

        // Getters y Setters

        public String getComponentName() {
            return componentName;
        }

        public void setComponentName(String componentName) {
            this.componentName = componentName;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getSubscriptionName() {
            return subscriptionName;
        }

        public void setSubscriptionName(String subscriptionName) {
            this.subscriptionName = subscriptionName;
        }
}