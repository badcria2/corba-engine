package corba.engine.rules;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Rule {

    @Id
    private String id; // Campo para almacenar el ID de la regla

    private String packageName;
    private List<String> imports;
    private String name;
    private List<String> conditions;

    private List<String> actions;

}