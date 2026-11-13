package hotel.dao;

import hotel.modelo.reserva.Reserva;

public interface ReservaEscritura {

    Reserva insertar(Reserva reserva, Integer idUsuario);

    void cambiarEstado(int idReserva, String estado, Integer idUsuario, String detalle);
}
