package corba.engine.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import corba.engine.models.KafkaData;
import corba.engine.response.GraphQLResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class GraphQLService {

    private static final Logger logger = LoggerFactory.getLogger(GraphQLService.class);
    private static final String CONTENT_TYPE = "application/json";
    private final WebClient webClient;

    // Constructor que inicializa WebClient con la URL base
    public GraphQLService(WebClient.Builder webClientBuilder, @Value("${graphql.endpoint:http://10.95.90.64:10000/oc/graphql}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        logger.info("GraphQLService inicializado con URL base: {}", baseUrl);
    }

    // Método genérico para realizar consultas GraphQL
    private Mono<GraphQLResponse> executeQuery(String query) {
        logger.debug("Ejecutando consulta GraphQL: {}", query);

        return webClient.post()
                .header("Content-Type", CONTENT_TYPE)
                .bodyValue("{\"query\":\"" + query + "\"}")
                .retrieve()
                .bodyToMono(GraphQLResponse.class)
                .doOnNext(response -> logger.info("Respuesta GraphQL recibida: {}", response))
                .onErrorResume(error -> {
                    logger.error("Error al consultar GraphQL: {}", error.getMessage(), error);
                    return Mono.error(new RuntimeException("Error al consultar GraphQL: " + error.getMessage()));
                })
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2))
                        .doBeforeRetry(retrySignal -> logger.warn("Reintentando consulta GraphQL. Intento: {}", retrySignal.totalRetries() + 1)));
    }

    // Método para consultar todos los grupos disponibles
    public Mono<GraphQLResponse> getAllAvailableGroups() {
        String query = "{ getAllAvailableGroups }";
        return executeQuery(query);
    }

    // Método para consultar elementos de red por grupo
    public Mono<GraphQLResponse> getAllNetworkElementsByGroup(String group) {
        String query = String.format("query { getAllNetworkElementsByGroup(group: \\\"%s\\\") }", group);
        return executeQuery(query);
    }
}
