package corba.engine.config;

import com.mongodb.client.*;
import corba.engine.models.Persona;
import corba.engine.repository.RuleRepository;
import corba.engine.rules.EventCorbaService;
import corba.engine.rules.Rule;
import org.bson.Document;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.logging.Logger;

@Configuration
public class DroolsConfiguration {
    private static final Logger logger = Logger.getLogger(DroolsConfiguration.class.getName());
    @Value("${spring.data.mongodb.uri}")
    private String mongoURI;
    @Autowired
    RuleRepository repository;
    @Bean
    public KieContainer kieContainer() {
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        try (MongoClient mongoClient = MongoClients.create(mongoURI)) {
            MongoDatabase database = mongoClient.getDatabase("drools");
            MongoCollection<Document> collection = database.getCollection("drools_rules");
            MongoCursor<Document> cursor = collection.find().iterator();
            while (cursor.hasNext()) {
                Document ruleDoc = cursor.next();
                Rule rule = mapDocumentToRule(ruleDoc); // Método para mapear Document a Rul
                String fileName = rule.getName() + ".drl"; // Naming the file
                kieFileSystem.write("src/main/resources/rules/" + fileName, buildRuleContent(rule));


            }
        }
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem).buildAll();
        KieModule kieModule = kieBuilder.getKieModule();

        return kieServices.newKieContainer(kieModule.getReleaseId());
    }

    private String buildRuleContent(Rule rule) {
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("package ").append(rule.getPackageName()).append("\n\n");

        // Agrega los imports
        for (String importLine : rule.getImports()) {
            contentBuilder.append("import ").append(importLine).append(";\n");
        }

        contentBuilder.append("declare KafkaData").append("\n");
        contentBuilder.append("     tags : Tags").append("\n");
        contentBuilder.append("     values : Map").append("\n");
        contentBuilder.append("     name  : String").append("\n");
        contentBuilder.append("     timestamp   : Long").append("\n");
        contentBuilder.append("end").append("\n");

        contentBuilder.append("rule \"").append(rule.getName()).append("\"\n");
        contentBuilder.append("  when\n");

        // Agrega las condiciones
        for (String condition : rule.getConditions()) {
            contentBuilder.append("    ").append(condition).append("\n");
        }

        contentBuilder.append("  then\n");

        // Agrega las acciones
        for (String action : rule.getActions()) {
            contentBuilder.append("    ").append(action).append("\n");
        }

        contentBuilder.append("end\n");
        logger.info("DRL::\n" + contentBuilder.toString());

        return contentBuilder.toString();
    }

    private String buildRuleContent_temp(Rule rule) {
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("package ").append(rule.getPackageName()).append("\n\n");
        contentBuilder.append("import ").append(Persona.class.getCanonicalName()).append(";\n"); // Importa la clase Persona
        contentBuilder.append("import ").append(EventCorbaService.class.getCanonicalName()).append(";\n"); // Importa el servicio
        contentBuilder.append("rule \"").append(rule.getName()).append("\"\n");
        contentBuilder.append("  when\n");
        contentBuilder.append("    $persona : Persona(edad < 18)\n"); // Modifica la condición
        contentBuilder.append("    $personService : ").append(EventCorbaService.class.getSimpleName()).append("()\n"); // Declara el servicio
        contentBuilder.append("  then\n");
        contentBuilder.append("    $personService.enviarcampania($persona);\n"); // Usa el servicio para enviar la campaña
        contentBuilder.append("end\n");
        logger.info("DRL::\n" + contentBuilder.toString());

        return contentBuilder.toString();
    }
    private Rule mapDocumentToRule(Document document) {
        String id = document.getObjectId("_id").toString();
        String packageName = document.getString("package");
        String name = document.getString("name");
        List<String> actions = (List<String>) document.get("actions");
        List<String> condition = (List<String>) document.get("condition");
        List<String> imports = (List<String>) document.get("imports");

        return new Rule(id, packageName,imports, name, condition, actions);
    }
}
