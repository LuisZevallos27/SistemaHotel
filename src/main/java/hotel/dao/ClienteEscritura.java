package hotel.dao;

import hotel.modelo.Cliente;

public interface ClienteEscritura {

    void insertar(Cliente c);

    void actualizar(Cliente c);

    void desactivar(int idCliente);
}
