package hotel.dao;

import hotel.modelo.Cliente;
import java.sql.SQLException;
import java.util.List;

public interface ClienteDAO {

    List<Cliente> listar() throws SQLException;

    void insertar(Cliente c) throws SQLException;

    void actualizar(Cliente c) throws SQLException;

    void desactivar(int idCliente) throws SQLException;

    boolean existeDocumento(String documento, int idExcluido) throws SQLException;
}
