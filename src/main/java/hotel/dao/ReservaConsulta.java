package hotel.dao;

import hotel.reserva.RegistroHistorial;
import hotel.reserva.Reserva;
import java.util.List;
import java.util.Optional;

public interface ReservaConsulta {

    List<Reserva> listar(String estado);

    Optional<Reserva> buscarPorId(int idReserva);

    List<RegistroHistorial> historial(int idReserva);
}
