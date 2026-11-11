package hotel.dao;

import hotel.reserva.RegistroHistorial;
import hotel.reserva.Reserva;
import java.util.List;

public interface ReservaConsulta {

    List<Reserva> listar(String estado);

    List<RegistroHistorial> historial(int idReserva);
}
