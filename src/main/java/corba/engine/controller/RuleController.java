package corba.engine.controller;

import corba.engine.dao.entity.*;
import corba.engine.dao.repository.ClienteRepository;
import corba.engine.dao.repository.ClienteServicioRepository;
import corba.engine.dao.response.ClienteInfoDTO;
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

    @Autowired
    private ClienteServicioRepository clienteServicioRepository;
    @Autowired
    private ClienteRepository clienteRepository;

    @GetMapping("/cliente/{dni}")
    public ResponseEntity<?> getClienteInfo(@PathVariable String dni) {
        Optional<Cliente> optionalCliente = clienteRepository.findByDni(dni);
        if (optionalCliente.isPresent()) {
            Cliente cliente = optionalCliente.get();
            List<Servicio> servicios = clienteRepository.findServiciosByCliente(cliente.getId());
            List<Enlace> enlaces = clienteRepository.findEnlacesByCliente(cliente.getId());
            List<Puerto> puertos = clienteRepository.findPuertosByCliente(cliente.getId());

            // Puedes devolver toda la información en forma de objeto DTO o Map
            // Aquí se devuelve como ResponseEntity
            ClienteInfoDTO ClienteInfoDTO = new ClienteInfoDTO(cliente, servicios, enlaces, puertos);
            ruleService.executeRulesWithClient(ClienteInfoDTO);
            return ResponseEntity.ok(ClienteInfoDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
