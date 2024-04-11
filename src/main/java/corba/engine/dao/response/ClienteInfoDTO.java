package corba.engine.dao.response;

import corba.engine.dao.entity.Cliente;
import corba.engine.dao.entity.Enlace;
import corba.engine.dao.entity.Puerto;
import corba.engine.dao.entity.Servicio;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClienteInfoDTO {
    private Cliente cliente;
    private List<Servicio> servicios;
    private List<Enlace> enlaces;
    private List<Puerto> puertos;
}
