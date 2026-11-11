package hotel.servicio;

import hotel.canal.Canal;
import hotel.excepcion.NegocioException;
import hotel.habitacion.HabitacionLibre;
import hotel.reserva.RegistroHistorial;
import hotel.reserva.Reserva;
import hotel.reserva.ReservaMemento;
import hotel.reserva.SolicitudReserva;
import java.time.LocalDate;
import java.util.List;

public interface ReservaService {

    List<String> tiposHabitacion();

    List<Canal> canales();

    List<String> estados();

    List<HabitacionLibre> habitacionesDisponibles(String tipoHabitacion, LocalDate entrada,
            LocalDate salida) throws NegocioException;

    Reserva crear(SolicitudReserva solicitud, Integer idUsuario) throws NegocioException;

    double anticipoRequerido(Reserva reserva);

    List<Reserva> listar(String estado);

    List<RegistroHistorial> historial(int idReserva);

    void confirmar(Reserva reserva, Integer idUsuario) throws NegocioException;

    boolean cancelar(Reserva reserva, Integer idUsuario) throws NegocioException;

    void marcarNoShow(Reserva reserva, Integer idUsuario) throws NegocioException;

    void restaurarEstado(Reserva reserva, ReservaMemento memento, Integer idUsuario,
            String detalle) throws NegocioException;
}
