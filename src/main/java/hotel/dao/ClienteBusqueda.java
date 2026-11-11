package hotel.dao;

import hotel.modelo.Cliente;
import java.util.Optional;

public interface ClienteBusqueda {

    Optional<Cliente> buscarPorDocumento(String documento);
}
