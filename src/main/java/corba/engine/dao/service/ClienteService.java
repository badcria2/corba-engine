package corba.engine.dao.service;

import corba.engine.dao.entity.Cliente;
import corba.engine.dao.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    Optional<Cliente> buscarPorDni(Cliente cliente) {
        return clienteRepository.findByDni(cliente.getDni());
    }
}