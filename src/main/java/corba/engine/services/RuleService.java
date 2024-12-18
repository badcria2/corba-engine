package corba.engine.services;

import corba.engine.config.RuleServiceMongo;
import corba.engine.models.KafkaData;
import corba.engine.models.Persona;
import corba.engine.rules.EventCorbaService;
import corba.engine.rules.Rule;
import org.kie.api.KieBase;
import org.kie.api.definition.KiePackage;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
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
    @PostConstruct
    public void initializeKieSession() {
        try {
            kieSession = kieContainer.newKieSession();
            logger.info("kieSession inicializada correctamente.");
        } catch (Exception e) {
            logger.severe("Error al inicializar kieSession: " + e.getMessage());
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

            listRules(kieSession.getKieBase());

            executeWithSession(kieSession -> {
                kieSession.insert(kafkaData);
                kieSession.insert(actionService);
                logFactsInSession(kieSession);
                int reglasEjecutadas = kieSession.fireAllRules();
                logger.info("Reglas ejecutadas: " + reglasEjecutadas);
            });
        }  catch (Exception e) {
        logger.severe("Error general procesando el mensaje: " + e.getMessage());
        e.printStackTrace(); // Esto imprimirá el stacktrace completo
    }
    finally {
            lock.readLock().unlock();
        }
    }
    /**
     * Método para listar todas las reglas cargadas en la KieBase
     */
    private void listRules(KieBase kieBase) {
        logger.info("=== Reglas cargadas en KieBase ===");
        kieBase.getKiePackages().forEach(kiePackage -> {
            logger.info("Paquete: " + kiePackage.getName());
            kiePackage.getRules().forEach(rule ->
                    logger.info("Regla estándar Drools: " + rule.getName() +" :::  " +  rule.getMetaData().toString())
            );
        });

        logger.info("=== Reglas personalizadas cargadas en la sesión ===");
        if (kieSession != null) {
            kieSession.getObjects()
                    .stream()
                    .filter(obj -> obj instanceof Rule) // Filtrar objetos de tipo Rule
                    .map(obj -> (Rule) obj) // Convertir a Rule
                    .forEach(rule -> {
                        logger.info("ID: " + rule.getId());
                        logger.info("Paquete: " + rule.getPackageName());
                        logger.info("Imports: " + rule.getImports());
                        logger.info("Nombre: " + rule.getName());
                        logger.info("Condiciones: " + rule.getConditions());
                        logger.info("Acciones: " + rule.getActions());
                        logger.info("-----------------------------");
                    });
        } else {
            logger.warning("La sesión no está inicializada. No se pueden listar las reglas personalizadas.");
        }
        logger.info("=========================================");
    }

    /**
     * Recarga las reglas desde MongoDB, creando una nueva sesión.
     */
    public void reloadRules() {
        lock.writeLock().lock();
        try {
            logger.info("Recargando reglas desde MongoDB...");
            if (kieSession != null) {
                kieSession.dispose(); // Liberar recursos de la sesión anterior
            }

            kieSession = kieContainer.newKieSession(); // Crear una nueva sesión
            List<Rule> rules = ruleServiceMongo.loadRulesFromMongo();
            for (Rule rule : rules) {
                kieSession.insert(rule);
                logger.info("Regla recargada: " + rule.getName());
            }

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
