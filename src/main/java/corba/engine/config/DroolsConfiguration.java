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

    @Autowired
    private RuleServiceMongo ruleService;

    @Bean
    public KieContainer kieContainer() {
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

        // Cargar reglas desde el servicio
        List<Rule> rules = ruleService.loadRulesFromMongo();
        for (Rule rule : rules) {
            String drlContent = ruleService.buildRuleContent(rule);
            String fileName = "src/main/resources/rules/" + rule.getName() + ".drl";
            kieFileSystem.write(fileName, drlContent);
        }

        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem).buildAll();
        if (kieBuilder.getResults().hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
            throw new RuntimeException("Error al compilar las reglas: " + kieBuilder.getResults());
        }

        KieModule kieModule = kieBuilder.getKieModule();
        return kieServices.newKieContainer(kieModule.getReleaseId());
    }
}
