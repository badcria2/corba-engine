package corba.engine.rules;

import corba.engine.models.Persona;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;
@Component
public class CustomAgendaEventListener extends DefaultAgendaEventListener {
    private static final Logger logger = Logger.getLogger(CustomAgendaEventListener.class.getName());

   // private final EventCorbaService actionService;

    public CustomAgendaEventListener(EventCorbaService eventCorbaService) {
        logger.info("Se crea el listener: " + CustomAgendaEventListener.class.getName());
        //this.actionService = eventCorbaService;
    }

    @Override
    public void afterMatchFired(AfterMatchFiredEvent event) {
        logger.info("afterMatchFired");
        String ruleName = event.getMatch().getRule().getName();
        switch (ruleName) {
            case "ValidarEdad":
                Persona personaMenor = (Persona) event.getMatch().getObjects().toArray()[0];
                logger.info("Regla activada: " + ruleName);
                logger.info("Persona antes de enviar la campaña: " + personaMenor);
                //actionService.enviarcampania(personaMenor); // Usar el método en el servicio
                logger.info("Persona después de enviar la campaña: " + personaMenor);
                break;
            default:
                logger.info("No hay campaña");
                break;
        }
    }
}
