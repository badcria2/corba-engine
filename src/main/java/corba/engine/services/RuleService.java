package corba.engine.services;

import corba.engine.models.KafkaData;
import corba.engine.models.Persona;
import corba.engine.rules.EventCorbaService;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class RuleService {
    private static final Logger logger = Logger.getLogger(RuleService.class.getName());

    @Autowired
    private EventCorbaService actionService;
    @Autowired
    private KieSession kieSession;
    public void executeRulesWithPerson(Persona persona) {

        logger.info("Ejecutando reglas...");
        kieSession.insert(persona);
        kieSession.insert(actionService); // Insert the service if needed in the rules

        int reglasEjecutadas = kieSession.fireAllRules();
        logger.info("Reglas ejecutadas: " + reglasEjecutadas + "   reglass");

        removePersonsFromSession();
    }

    public void executeRulesWithEventKafka(KafkaData kafkaData) {

        logger.info("Ejecutando reglas...");
        kieSession.insert(kafkaData);
        kieSession.insert(actionService); // Insert the service if needed in the rules

        Double dato = new Double( Double.parseDouble(kafkaData.getValues().get("/components/component/optical-channel/state/target-output-power\\")));
        System.out.println(" Dato Valor double :: " + dato);
        int reglasEjecutadas = kieSession.fireAllRules();
        logger.info("Reglas ejecutadas: " + reglasEjecutadas + "   reglass");
        removePersonsFromSession();
    }

    private void removePersonsFromSession() {
        // Agrega registros de depuración para verificar los hechos antes de eliminarlos de la sesión
        logger.info("Hechos antes de eliminarlos de la sesión de Drools:");
        kieSession.getObjects().forEach(fact -> logger.info(fact.toString()));

        for (Object factObject : kieSession.getObjects()) {
            if (factObject instanceof Persona) {
                kieSession.delete(kieSession.getFactHandle(factObject));
            }
        }

        // Agrega registros de depuración para verificar los hechos después de eliminarlos de la sesión
        logger.info("Hechos después de eliminarlos de la sesión de Drools:");
        kieSession.getObjects().forEach(fact -> logger.info(fact.toString()));
    }

    public void enviarcampania(Persona persona) {
        actionService.enviarcampania(persona);
    }


}
