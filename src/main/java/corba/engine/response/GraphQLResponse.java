package corba.engine.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphQLResponse {

    private Data data;

    // Getters y Setters
    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {

        private List<String> getAllAvailableGroups;

        @JsonProperty("getAllNetworkElementsByGroup")
        private List<NetworkElement> getAllNetworkElementsByGroup;

        // Getters y Setters
        public List<String> getGetAllAvailableGroups() {
            return getAllAvailableGroups;
        }

        public void setGetAllAvailableGroups(List<String> getAllAvailableGroups) {
            this.getAllAvailableGroups = getAllAvailableGroups;
        }

        public List<NetworkElement> getAllNetworkElementsByGroup() {
            return getAllNetworkElementsByGroup;
        }

        public void setGetAllNetworkElementsByGroup(List<NetworkElement> getAllNetworkElementsByGroup) {
            this.getAllNetworkElementsByGroup = getAllNetworkElementsByGroup;
        }
    }
}
