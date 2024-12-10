package corba.engine.response;

import java.util.List;

public class GraphQLResponse {
    private Data data;

    // Getters y Setters
    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private List<String> getAllAvailableGroups;
        private List<NetworkElement> getAllNetworkElementsByGroup;

        // Getters y Setters
        public List<String> getGetAllAvailableGroups() {
            return getAllAvailableGroups;
        }
        public void setGetAllAvailableGroups(List<String> getAllAvailableGroups) {
            this.getAllAvailableGroups = getAllAvailableGroups;
        }

        public void setGetAllNetworkElementsByGroup(List<String> getAllNetworkElementsByGroup) {
            this.getAllAvailableGroups = getAllNetworkElementsByGroup;
        }
        public List<NetworkElement> getAllNetworkElementsByGroup() {
            return getAllNetworkElementsByGroup;
        }


    }
}
