package corba.engine.config;

import com.mongodb.MongoException;
import com.mongodb.client.*;
import corba.engine.repository.RuleRepository;
import corba.engine.rules.CustomAgendaEventListener;
import corba.engine.rules.PersonService;
import corba.engine.rules.Rule;
import org.bson.Document;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Configuration
public class DroolsSessionConfiguration {
    private static final Logger logger = Logger.getLogger(DroolsSessionConfiguration.class.getName());

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private PersonService actionService;

    @Value("${spring.data.mongodb.uri}")
    private String mongoURI;

    @Autowired
    private KieContainer kieContainer;

    @Bean
    public KieSession kieSession() {
        logger.info("Creando una nueva sesión de Drools...");

        KieSession kieSession = kieContainer.newKieSession();
        loadRulesFromMongo(kieSession);
        logger.info("Creando un listener");
        kieSession.addEventListener(new CustomAgendaEventListener(actionService));
        return kieSession;
    }

    @PreDestroy
    public void closeSession() {
        if (kieContainer != null) {
            kieContainer.dispose();
        }
    }


    private void loadRulesFromMongo(KieSession kieSession) {
        try (MongoClient mongoClient = MongoClients.create(mongoURI)) {
            MongoDatabase database = mongoClient.getDatabase("drools");
            MongoCollection<Document> collection = database.getCollection("drools_rules");

            MongoCursor<Document> cursor = collection.find().iterator();
            while (cursor.hasNext()) {
                Document ruleDoc = cursor.next();
                String packageName = ruleDoc.getString("package");
                String id = ruleDoc.getObjectId("_id").toString();
                String ruleName = ruleDoc.getString("name");
                List<String> actions = (List<String>) ruleDoc.get("actions");
                List<String> condition = (List<String>) ruleDoc.get("condition");
                List<String> imports = (List<String>) ruleDoc.get("imports");

                Rule rule = new Rule(id, packageName, imports, ruleName, condition, actions);
                kieSession.insert(rule);
            }
        } catch (MongoException e) {
            // Manejar excepciones de MongoDB
            logger.info("Error al cargar las reglas desde MongoDB: " + e.getMessage());
            throw new RuntimeException("Error al cargar las reglas desde MongoDB", e);
        } catch (Exception e) {
            // Manejar otras excepciones
            logger.info("Error al cargar las reglas: " + e.getMessage());
            throw new RuntimeException("Error al cargar las reglas", e);
        }
    }

}
