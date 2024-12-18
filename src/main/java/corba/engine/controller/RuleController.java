package corba.engine.controller;

import corba.engine.config.RuleServiceMongo;
import corba.engine.models.Persona;
import corba.engine.rules.Rule;
import corba.engine.services.RuleService;
import corba.engine.services.RuleServiceMaintenance;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/rules")
public class RuleController {
    private static final Logger logger = Logger.getLogger(RuleController.class.getName());

    private final RuleServiceMaintenance ruleService;
    private final RuleService ruleServicePerson ;

    @Autowired
    public RuleController(RuleServiceMaintenance ruleService, RuleService ruleServicePerson,RuleServiceMongo ruleServiceMongo) {
        this.ruleService = ruleService;
        this.ruleServicePerson = ruleServicePerson;
    }
    @PostMapping("/validar-edad")
    public @ResponseBody Persona validarEdad(@RequestBody Persona persona) {
        logger.info("Persona recibida para validar edad: " + persona);

        ruleServicePerson.reloadRules();
        ruleServicePerson.executeRulesWithPerson(persona);
        return persona;
    }

    @PostMapping
    public ResponseEntity<Rule> createRule(@RequestBody Rule rule) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ruleService.createRule(rule));
    }

    @GetMapping
    public ResponseEntity<List<Rule>> getAllRules() {
        return ResponseEntity.ok(ruleService.getAllRules());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rule> getRuleById(@PathVariable String id) {
        return ruleService.getRuleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Rule> updateRule(@PathVariable String id, @RequestBody Rule rule) {
        Rule updatedRule = ruleService.updateRule(id, rule);
        ruleServicePerson.reloadRules();
        return ResponseEntity.ok(updatedRule);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable String id) {
        ruleService.deleteRule(id);
        ruleServicePerson.reloadRules();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Rule>> searchRulesByName(@RequestParam String pattern) {
        List<Rule> rules = ruleService.searchRulesByNamePattern(".*" + pattern + ".*");

        ruleServicePerson.reloadRules();
        return ResponseEntity.ok(rules);
    }

}
