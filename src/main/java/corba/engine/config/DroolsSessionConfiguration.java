package corba.engine.config;

import corba.engine.rules.CustomAgendaEventListener;
import corba.engine.rules.EventCorbaService;
import corba.engine.rules.Rule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.logging.Logger;

@Configuration
public class DroolsSessionConfiguration {
    private static final Logger logger = Logger.getLogger(DroolsSessionConfiguration.class.getName());

    @Autowired
    private EventCorbaService actionService;

    @Autowired
    private KieContainer kieContainer;

    private KieSession kieSession;

    @Autowired
    private RuleServiceMongo ruleService; // Inyectar el servicio de reglas
   /* @Bean
    public synchronized KieSession kieSession() {
        logger.info("Creando una nueva sesión de Drools...");
        kieSession = kieContainer.newKieSession();
        kieSession.getObjects().forEach(fact -> kieSession.delete(kieSession.getFactHandle(fact)));
        loadRulesFromMongo(kieSession);


        // Agregar listeners personalizados
        kieSession.addEventListener(new CustomAgendaEventListener(actionService));
        return kieSession;
    }*//*
    private void loadRulesFromMongo(KieSession kieSession) {
        try {
            // Llamamos al servicio que carga las reglas desde MongoDB
            List<Rule> rules = ruleService.loadRulesFromMongo();

            // Limpiar las reglas previas de la sesión antes de insertar las nuevas
            kieSession.getObjects().forEach(fact -> kieSession.delete(kieSession.getFactHandle(fact)));

            // Insertar las nuevas reglas en la sesión
            for (Rule rule : rules) {
                kieSession.insert(rule);
                logger.info("Regla cargada en la sesión: " + rule.getName());
            }

        } catch (Exception e) {
            logger.severe("Error al cargar las reglas desde MongoDB: " + e.getMessage());
            throw new RuntimeException("Error al cargar las reglas desde MongoDB", e);
        }
    }*/


    @PreDestroy
    public void closeSession() {
        if (kieSession != null) {
            kieSession.dispose();
            logger.info("Sesión de Drools cerrada.");
        }
    }
}