package corba.engine.dao.repository;
import corba.engine.dao.entity.ClienteServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteServicioRepository extends JpaRepository<ClienteServicio, Long> {
}