package hotel.dao;

import hotel.reserva.Reserva;

public interface ReservaEscritura {

    Reserva insertar(Reserva reserva, Integer idUsuario);

    void cambiarEstado(int idReserva, String estado, Integer idUsuario, String detalle);
}
