package corba.engine.config;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import corba.engine.rules.Rule;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class RuleServiceMongo {
    private static final Logger logger = Logger.getLogger(RuleServiceMongo.class.getName());

    @Value("${spring.data.mongodb.uri}")
    private String mongoURI;

    public List<Rule> loadRulesFromMongo() {
        List<Rule> rules = new ArrayList<>();

        try (MongoClient mongoClient = MongoClients.create(mongoURI)) {
            MongoDatabase database = mongoClient.getDatabase("drools");
            MongoCollection<Document> collection = database.getCollection("drools_rules");

            for (Document doc : collection.find()) {
                rules.add(mapDocumentToRule(doc));
            }
        } catch (Exception e) {
            logger.severe("Error al cargar reglas de MongoDB: " + e.getMessage());
            throw new RuntimeException("Error al cargar reglas de MongoDB", e);
        }

        return rules;
    }

    public String buildRuleContent(Rule rule) {
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("package ").append(rule.getPackageName()).append("\n\n");

        // Agregar imports
        for (String importLine : rule.getImports()) {
            contentBuilder.append("import ").append(importLine).append(";\n");
        }

        contentBuilder.append("rule \"").append(rule.getName()).append("\"\n");
        contentBuilder.append("  when\n");
        for (String condition : rule.getConditions()) {
            contentBuilder.append("    ").append(condition).append("\n");
        }
        contentBuilder.append("  then\n");
        for (String action : rule.getActions()) {
            contentBuilder.append("    ").append(action).append("\n");
        }
        contentBuilder.append("end\n");

        logger.info("DRL generado:\n" + contentBuilder);
        return contentBuilder.toString();
    }

    private Rule mapDocumentToRule(Document document) {
        String id = document.getObjectId("_id").toString();
        String packageName = document.getString("packageName");
        String name = document.getString("name");
        List<String> actions = (List<String>) document.get("actions");
        List<String> conditions = (List<String>) document.get("conditions");
        List<String> imports = (List<String>) document.get("imports");

        return new Rule(id, packageName, imports, name, conditions, actions);
    }
}