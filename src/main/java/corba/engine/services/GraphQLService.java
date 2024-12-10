package corba.engine.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import corba.engine.response.GraphQLResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class GraphQLService {

    private final WebClient webClient;

    // Constructor que inicializa WebClient con la URL base
    public GraphQLService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://10.95.90.64:10000/oc/graphql").build();
    }

    // Método que consulta los grupos disponibles
    public Mono<GraphQLResponse> getAllAvailableGroups() {
        String query = "{ getAllAvailableGroups }";

        // Realiza la consulta GraphQL y mapea la respuesta a GraphQLResponse
        return webClient.post()
                .bodyValue("{\"query\":\"" + query + "\"}")
                .header("Content-Type", "application/json")
                .retrieve()
                .bodyToMono(GraphQLResponse.class)  // Mapea la respuesta a la clase GraphQLResponse
                .onErrorResume(error -> Mono.error(new RuntimeException("Error al consultar GraphQL: " + error.getMessage())));
    }

    public Mono<GraphQLResponse> getAllNetworkElementsByGroup(String group) {
        String query = String.format("query MyQuery { getAllNetworkElementsByGroup(group: \"%s\") { name managementIp } }", group);

        return webClient.post()
                .header("Content-Type", "application/json")
                .bodyValue("{\"query\":\"" + query + "\"}")
                .retrieve()
                .bodyToMono(String.class) // Leer como String para inspección
                .doOnNext(json -> System.out.println("Respuesta GraphQL: " + json)) // Log de la respuesta
                .map(json -> {
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        return mapper.readValue(json, GraphQLResponse.class); // Deserializar manualmente
                    } catch (Exception e) {
                        throw new RuntimeException("Error deserializando JSON: " + e.getMessage(), e);
                    }
                })
                .onErrorResume(error -> Mono.error(new RuntimeException("Error al consultar GraphQL: " + error.getMessage())));
    }


}
