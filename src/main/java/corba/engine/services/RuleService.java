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
     * Ejecuta lógica personalizada con una nueva sesión de Drools.
     *
     * @param sessionConsumer Lógica personalizada que se ejecutará con la sesión.
     */
    private void executeWithSession(Consumer<KieSession> sessionConsumer) {
        lock.readLock().lock();
        try {
            // Verificar si la sesión es válida antes de usarla
            if (kieSession == null || isSessionDisposed()) {
                // Crear una nueva sesión si es necesario
                kieSession = kieContainer.newKieSession();
                logger.info("Nueva kieSession creada.");
            }

            sessionConsumer.accept(kieSession);
        } catch (Exception e) {
            logger.severe("Error durante la ejecución de reglas: " + e.getMessage());
        } finally {
            // No cerramos la sesión aquí, ya que puede ser necesaria en el siguiente ciclo
            lock.readLock().unlock();
        }
    }

    /**
     * Verifica si la sesión de Drools ha sido descartada (dispose).
     * Este método es una solución para garantizar que la sesión no se esté utilizando después de ser cerrada.
     */
    private boolean isSessionDisposed() {
        // Si no hay sesión o la sesión ha sido previamente cerrada (dispose), retornamos true
        return !kieSession.getObjects().iterator().hasNext();
    }

    /**
     * Ejecuta reglas utilizando un objeto de tipo Persona.
     *
     * @param persona Objeto Persona que se insertará en la sesión.
     */
    public void executeRulesWithPerson(Persona persona) {
        executeWithSession(kieSession -> {
            kieSession.insert(persona);
            kieSession.insert(actionService);
            logFactsInSession(kieSession);
            int reglasEjecutadas = kieSession.fireAllRules();
            logger.info("Reglas ejecutadas: " + reglasEjecutadas);
        });
    }

    /**
     * Ejecuta reglas utilizando un objeto de tipo KafkaData.
     *
     * @param kafkaData Objeto KafkaData que se insertará en la sesión.
     */
    public void executeRulesWithEventKafka(KafkaData kafkaData) {
        executeWithSession(kieSession -> {
            listRules(kieSession.getKieBase());
            kieSession.insert(kafkaData);
            kieSession.insert(actionService);
            logFactsInSession(kieSession);
            int reglasEjecutadas = kieSession.fireAllRules();
            logger.info("Reglas ejecutadas: " + reglasEjecutadas);
        });
    }

    /**
     * Método para listar todas las reglas cargadas en la KieBase
     */
    private void listRules(KieBase kieBase) {
        logger.info("=== Reglas cargadas en KieBase ===");
        kieBase.getKiePackages().forEach(kiePackage -> {
            logger.info("Paquete: " + kiePackage.getName());
            kiePackage.getRules().forEach(rule -> {
                logger.info("Regla estándar Drools: " + rule.getName() + " :::  " + rule.getMetaData().toString());
            });
        });
        logger.info("=========================================");
    }

    /**
     * Recarga las reglas desde MongoDB, creando una nueva sesión si es necesario.
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
     * Registra los hechos presentes en la sesión.
     *
     * @param session Sesión de Drools.
     */
    private void logFactsInSession(KieSession session) {
        logger.info("Hechos presentes en la sesión:");
        session.getObjects().forEach(fact -> logger.info("Hecho: " + fact));
    }
}
