package corba.engine.services;


import corba.engine.repository.RuleRepositoryMaintenance;
import corba.engine.rules.Rule;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RuleServiceMaintenance {
    private final RuleRepositoryMaintenance ruleRepository;

    public RuleServiceMaintenance(RuleRepositoryMaintenance ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    public Rule createRule(Rule rule) {
        return ruleRepository.save(rule);
    }

    public List<Rule> getAllRules() {
        return ruleRepository.findAll();
    }

    public Optional<Rule> getRuleById(String id) {
        return ruleRepository.findById(id);
    }
    public List<Rule> searchRulesByNamePattern(String pattern) {
        return ruleRepository.findByNameRegex(pattern);
    }
    public Rule updateRule(String id, Rule rule) {
        if (ruleRepository.existsById(id)) {
            rule.setId(id);
            return ruleRepository.save(rule);
        } else {
            throw new RuntimeException("Rule not found");
        }
    }

    public void deleteRule(String id) {
        ruleRepository.deleteById(id);
    }
}
