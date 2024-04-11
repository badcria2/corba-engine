package corba.engine.dao.repository;

import corba.engine.dao.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByDni(String dni);

    @Query("SELECT cs FROM ClienteServicio cs WHERE cs.cliente.id = :idCliente")
    List<ClienteServicio> findClienteServiciosByCliente(@Param("idCliente") Long idCliente);

    @Query("SELECT cs.servicio FROM ClienteServicio cs WHERE cs.cliente.id = :idCliente")
    List<Servicio> findServiciosByCliente(@Param("idCliente") Long idCliente);



    @Query("SELECT e FROM Enlace e JOIN EnlaceServicio es ON e.id = es.enlace.id JOIN ClienteServicio cs ON es.servicio.id = cs.servicio.id WHERE cs.cliente.id = :idCliente")
    List<Enlace> findEnlacesByCliente(Long idCliente);

    @Query("SELECT p FROM Puerto p JOIN ServicioPuerto e ON p.id = e.puerto.id JOIN Servicio es ON es.id = e.servicio.id JOIN NE nel ON p.ne.id = nel.id JOIN ClienteServicio  cs on es.id = cs.cliente.id WHERE cs.cliente.id = :idCliente")
    List<Puerto> findPuertosByCliente(Long idCliente);


}