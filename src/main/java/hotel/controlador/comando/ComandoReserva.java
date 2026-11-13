package hotel.controlador.comando;

import hotel.excepcion.NegocioException;

public interface ComandoReserva {

    void ejecutar() throws NegocioException;

    void deshacer() throws NegocioException;

    String descripcion();
}
