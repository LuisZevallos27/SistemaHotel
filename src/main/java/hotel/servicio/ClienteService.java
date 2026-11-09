package hotel.servicio;

import hotel.excepcion.NegocioException;
import hotel.modelo.Cliente;
import java.util.List;

public interface ClienteService {

    List<Cliente> listar();

    void guardar(Cliente c) throws NegocioException;

    void desactivar(int idCliente);

    List<String> tiposDocumento();
}
