package corba.engine.services;

import corba.engine.config.RuleServiceMongo;
import corba.engine.models.KafkaData;
import corba.engine.models.Persona;
import corba.engine.rules.EventCorbaService;
import corba.engine.rules.Rule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.logging.Logger;

@Service
public class RuleService {
    private static final Logger logger = Logger.getLogger(RuleService.class.getName());

    @Autowired
    private RuleServiceMongo ruleServiceMongo;

    @Autowired
    private EventCorbaService actionService;

    @Autowired
    private KieContainer kieContainer;

    private KieSession kieSession;
    private final ReadWriteLock lock = new ReentrantReadWriteLock(); // Bloqueo para concurrencia segura


    /**
     * Ejecuta lógica personalizada con una nueva sesión de Drools.
     *
     * @param sessionConsumer Lógica personalizada que se ejecutará con la sesión.
     */
    private void executeWithSession(Consumer<KieSession> sessionConsumer) {

        try {
            sessionConsumer.accept(kieSession);
        } finally {
            kieSession.dispose(); // Liberar recursos
            logger.info("Sesión de Drools cerrada.");
        }
    }

    /**
     * Ejecuta reglas utilizando un objeto de tipo Persona.
     *
     * @param persona Objeto Persona que se insertará en la sesión.
     */
    public void executeRulesWithPerson(Persona persona) {
        lock.readLock().lock();
        try {
            executeWithSession(kieSession -> {
                kieSession.insert(persona);
                kieSession.insert(actionService);
                logFactsInSession(kieSession);
                int reglasEjecutadas = kieSession.fireAllRules();
                logger.info("Reglas ejecutadas: " + reglasEjecutadas);
            });
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Ejecuta reglas utilizando un objeto de tipo KafkaData.
     *
     * @param kafkaData Objeto KafkaData que se insertará en la sesión.
     */
    public void executeRulesWithEventKafka(KafkaData kafkaData) {
        lock.readLock().lock();
        try {
            executeWithSession(kieSession -> {
                kieSession.insert(kafkaData);
                kieSession.insert(actionService);
                logFactsInSession(kieSession);
                int reglasEjecutadas = kieSession.fireAllRules();
                logger.info("Reglas ejecutadas: " + reglasEjecutadas);
            });
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Recarga las reglas desde MongoDB, creando una nueva sesión.
     */
    public void reloadRules() {
        lock.writeLock().lock();
        try {
            logger.info("Recargando reglas desde MongoDB...");
            if (kieSession != null) {
                kieSession.dispose();
            }
            kieSession = kieContainer.newKieSession();
            kieSession.getObjects().forEach(fact -> kieSession.delete(kieSession.getFactHandle(fact)));

            List<Rule> rules = ruleServiceMongo.loadRulesFromMongo();
            rules.forEach(rule -> {
                kieSession.insert(rule);
                logger.info("Regla recargada: " + rule.getName());
            });

            logger.info("Reglas recargadas exitosamente.");
        } catch (Exception e) {
            logger.severe("Error al recargar reglas: " + e.getMessage());
            throw new RuntimeException("Error al recargar reglas en Drools", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Envía una campaña utilizando el servicio de acciones personalizadas.
     *
     * @param persona Objeto Persona a utilizar en la campaña.
     */
    public void enviarcampania(Persona persona) {
        actionService.enviarcampania(persona);
    }

    /**
     * Registra las reglas activas en la sesión.
     *
     * @param session Sesión de Drools.
     */
    private void logRulesInSession(KieSession session) {
        logger.info("Reglas activas en la sesión:");
        kieSession.getKieBase().getKiePackages().forEach(kiePackage ->
                kiePackage.getRules().forEach(rule ->
                        logger.info("Regla activa: " + rule.getName())
                )
        );
    }

    /**
     * Registra los hechos presentes en la sesión.
     *
     * @param session Sesión de Drools.
     */
    private void logFactsInSession(KieSession session) {
        logger.info("Hechos presentes en la sesión:");
        session.getObjects().forEach(fact -> logger.info("Hecho: " + fact));
    }
}
