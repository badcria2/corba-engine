package corba.engine.repository;

import corba.engine.rules.Rule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuleRepositoryMaintenance extends MongoRepository<Rule, String> {
    List<Rule> findByNameRegex(String regex);

}