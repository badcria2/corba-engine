package corba.engine.services;

import corba.engine.config.RuleServiceMongo;
import corba.engine.models.KafkaData;
import corba.engine.models.Persona;
import corba.engine.rules.EventCorbaService;
import corba.engine.rules.Rule;
import org.kie.api.KieBase;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.KieServices;
import org.kie.internal.io.ResourceFactory;
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

    private final ReadWriteLock lock = new ReentrantReadWriteLock();


    public RuleService() {
        // Inicializar el KieContainer con una configuración inicial
        KieServices kieServices = KieServices.Factory.get();
        this.kieContainer = kieServices.getKieClasspathContainer();
    }
    private void executeWithNewSession(Consumer<KieSession> sessionConsumer) {
        KieSession newKieSession = null;
        try {
            lock.readLock().lock(); // Aseguramos que no haya escritura mientras ejecutamos
            if (kieContainer != null) {
                logger.info("KieContainer cargado correctamente.");
            } else {
                logger.severe("El KieContainer no está cargado.");
            }
            if (kieContainer.getKieBaseNames().isEmpty()) {
                logger.severe("No se encontraron KieBases en el KieContainer.");
            } else {
                kieContainer.getKieBaseNames().forEach(kieBaseName -> {
                    logger.info("KieBase encontrado: " + kieBaseName);
                });
            }

            newKieSession = kieContainer.newKieSession();
            if (newKieSession == null) {
                logger.severe("No se pudo crear una nueva KieSession.");
                return;
            }
            logger.info("Nueva KieSession creada para ejecución.");
            sessionConsumer.accept(newKieSession);
        } catch (Exception e) {
            logger.severe("Error durante la ejecución de reglas: " + e.getMessage());
        } finally {
            if (newKieSession != null) {
                newKieSession.dispose(); // Asegurar la eliminación de la sesión
                logger.info("KieSession desechada después de la ejecución.");
            }
            lock.readLock().unlock();
        }
    }

    public void executeRulesWithPerson(Persona persona) {
        executeWithNewSession(kieSession -> {
            kieSession.insert(persona);
            kieSession.insert(actionService);
            listRules(kieSession.getKieBase());
            logFactsInSession(kieSession);
            int reglasEjecutadas = kieSession.fireAllRules();
            logger.info("Reglas ejecutadas: " + reglasEjecutadas);
        });
    }

    public void executeRulesWithEventKafka(KafkaData kafkaData) {
        executeWithNewSession(kieSession -> {
            kieSession.insert(kafkaData);
            kieSession.insert(actionService);
            logFactsInSession(kieSession);
            int reglasEjecutadas = kieSession.fireAllRules();
            logger.info("Reglas ejecutadas: " + reglasEjecutadas);
        });
    }

    public void reloadRules() {
        lock.writeLock().lock();
        try {
            logger.info("Recargando reglas desde MongoDB...");

            // Cargar las reglas desde MongoDB
            List<Rule> rules = ruleServiceMongo.loadRulesFromMongo();
            logger.info("Total de reglas cargadas: " + rules.size());
            if (rules.isEmpty()) {
                logger.warning("No se encontraron reglas en MongoDB.");
                return;
            }

            // Crear un nuevo KieFileSystem para compilar las reglas
            KieServices kieServices = KieServices.Factory.get();
            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

            for (Rule rule : rules) {
                try {
                    kieFileSystem.write("src/main/resources/" + rule.getName() + ".drl",
                            ruleServiceMongo.buildRuleContent(rule));
                    logger.info("Regla cargada: " + rule.getName());
                } catch (Exception e) {
                    logger.severe("Error al procesar la regla: " + rule.getName() + ". Detalles: " + e.getMessage());
                }
            }

            // Compilar las reglas
            KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
            kieBuilder.buildAll();

            if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
                logger.severe("Errores al compilar las reglas: " + kieBuilder.getResults().toString());
                throw new IllegalStateException("Errores de compilación en las reglas.");
            }

            // Actualizar el KieContainer
            this.kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
            logger.info("Reglas recargadas exitosamente y KieContainer actualizado.");

        } catch (Exception e) {
            logger.severe("Error al recargar reglas: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void logFactsInSession(KieSession session) {
        logger.info("=== Hechos presentes en la sesión ===");
        session.getObjects().forEach(fact -> logger.info("Hecho: " + fact.toString()));
        logger.info("====================================");
    }

    private void listRules(KieBase kieBase) {
        logger.info("=== Reglas cargadas en KieBase ===");
        kieBase.getKiePackages().forEach(kiePackage -> {
            logger.info("Paquete: " + kiePackage.getName());
            kiePackage.getRules().forEach(rule ->
                    logger.info("Regla estándar Drools: " + rule.getName() + " ::: " + rule.getMetaData().toString()));
        });
        logger.info("===================================");
    }

    public void enviarcampania(Persona persona) {
        actionService.enviarcampania(persona);
        logger.info("Campaña enviada para persona: " + persona);
    }
}
