package corba.engine.controller;

import corba.engine.models.Persona;
import corba.engine.services.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@RestController
public class RuleController {
    private static final Logger logger = Logger.getLogger(RuleController.class.getName());

    private final RuleService ruleService;

    @Autowired
    public RuleController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @PostMapping("/validar-edad")
    public @ResponseBody Persona validarEdad(@RequestBody Persona persona) {
        logger.info("Persona recibida para validar edad: " + persona);
        ruleService.executeRulesWithPerson(persona);
        return persona;
    }


}
