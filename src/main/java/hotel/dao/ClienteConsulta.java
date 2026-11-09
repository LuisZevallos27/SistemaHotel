package hotel.dao;

import hotel.modelo.Cliente;
import java.util.List;

public interface ClienteConsulta {

    List<Cliente> listar();

    boolean existeDocumento(String documento, int idExcluido);
}
