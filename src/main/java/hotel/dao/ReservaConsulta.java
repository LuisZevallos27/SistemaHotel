package hotel.dao;

import hotel.modelo.reserva.RegistroHistorial;
import hotel.modelo.reserva.Reserva;
import java.util.List;
import java.util.Optional;

public interface ReservaConsulta {

    List<Reserva> listar(String estado);

    Optional<Reserva> buscarPorId(int idReserva);

    List<RegistroHistorial> historial(int idReserva);
}
