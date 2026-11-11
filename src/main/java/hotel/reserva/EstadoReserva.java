package hotel.reserva;

import hotel.excepcion.NegocioException;

public interface EstadoReserva {

    String nombre();

    EstadoReserva confirmar() throws NegocioException;

    EstadoReserva cancelar() throws NegocioException;

    EstadoReserva marcarNoShow() throws NegocioException;

    EstadoReserva registrarCheckIn() throws NegocioException;

    EstadoReserva registrarCheckOut() throws NegocioException;

    boolean admiteModificacion();
}
