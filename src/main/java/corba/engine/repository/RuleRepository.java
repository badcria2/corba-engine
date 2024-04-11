package corba.engine.repository;

import corba.engine.rules.Rule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface  RuleRepository extends MongoRepository<Rule, String> {
 Rule findByName(String name);
}