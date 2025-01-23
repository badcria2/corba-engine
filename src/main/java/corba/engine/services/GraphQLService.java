package corba.engine.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import corba.engine.models.KafkaData;
import corba.engine.response.GraphQLResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

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
                    System.err.println("Error al consultar GraphQL: {}" +  error.getMessage() +  error);
                    logger.error("Error al consultar GraphQL: {}", error.getMessage(), error);

                    System.out.println("DATOS CON ERROR:: " + query);
                    return Mono.error(new RuntimeException("Error al consultar GraphQL: " + error.getMessage()));
                })
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2))
                        .doBeforeRetry(retrySignal -> logger.warn("Reintentando consulta GraphQL. Intento: {}", retrySignal.totalRetries() + 1)));
    }
    private Mono<GraphQLResponse> executeQueryTemporal(String query) {
        logger.debug("Ejecutando consulta GraphQL: {}", query);

        // Construir la mutación manualmente
        String mutation = "mutation MyMutation { "
                + "executeRPCForNetworkElement(params: { "
                + "neName: \\\"TOL-7750SR-1-87\\\", "
                + "hostname: \\\"10.95.90.87\\\", "
                + "username: \\\"admin\\\", "
                + "password: \\\"admin\\\", "
                + "rpc: { rpc: \\\"\"\"<edit-config>\n"
                + "<target>\n"
                + "  <candidate/>\n"
                + "</target>\n"
                + "<config>\n"
                + "  <interfaces xmlns=\\\"http://openconfig.net/yang/interfaces\\\">\n"
                + "    <interface>\n"
                + "      <name>1/2/c1/1</name>\n"
                + "      <config>\n"
                + "        <name>1/2/c1/1</name>\n"
                + "        <type xmlns:ianaift=\\\"urn:ietf:params:xml:ns:yang:iana-if-type\\\">ianaift:ethernetCsmacd</type>\n"
                + "        <description>to_JNP-MX-304</description>\n"
                + "      </config>\n"
                + "    </interface>\n"
                + "  </interfaces>\n"
                + "</config>\n"
                + "</edit-config>\"\"\", commit: true } "
                + "}) }";

        // Crear el cuerpo de la solicitud
        String requestBody = "{\"query\": \"" + mutation + "\"}";

        // Usar WebClient para realizar la llamada
        return webClient.post()
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatus::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    logger.error("Error del servidor: {}", errorBody);
                                    return Mono.error(new RuntimeException("Error del servidor: " + errorBody));
                                })
                )
                .bodyToMono(GraphQLResponse.class)
                .doOnNext(response -> logger.info("Respuesta GraphQL recibida: {}", response))
                .onErrorResume(error -> {
                    logger.error("Error al consultar GraphQL: {}", error.getMessage(), error);
                    return Mono.error(new RuntimeException("Error al consultar GraphQL: " + error.getMessage()));
                });
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

    // Método para ejecutar RPC en un elemento de red
    public Mono<GraphQLResponse> executeRPCForNetworkElement(String neName, String hostname, String username, String password, String rpcConfig, boolean commit) {
        // Construir el query GraphQL
        String mutation = String.format(
                "mutation MyMutation { " +
                        "  executeRPCForNetworkElement(params: { " +
                        "    neName: \"%s\", " +
                        "    hostname: \"%s\", " +
                        "    username: \"%s\", " +
                        "    password: \"%s\", " +
                        "    rpc: { rpc: \"\"\"%s\"\"\", commit: %b }" +
                        "  }) " +
                        "}",
                neName, hostname, username, password, rpcConfig, commit
        );
        String query = String.format("mutation MyMutation { executeRPCForNetworkElement(params: { neName: \"%s\", hostname: \"%s\", username: \"%s\", password: \"%s\", rpc: { rpc: \"%s\", commit: %b } }) }",
                neName, hostname, username, password, rpcConfig, commit);
        String graphqlQuery = "{"
                + "\"query\": \"mutation MyMutation { "
                + "executeRPCForNetworkElement(params: { "
                + "neName: \\\"TOL-7750SR-1-87\\\", "
                + "hostname: \\\"10.95.90.87\\\", "
                + "username: \\\"admin\\\", "
                + "password: \\\"admin\\\", "
                + "rpc: { rpc: \\\"" + rpcConfig + "\\\", commit: true }"
                + "}) }\""
                + "}";

        // Llamar al método genérico para ejecutar el query
        return executeQueryTemporal(graphqlQuery);
    }

}
