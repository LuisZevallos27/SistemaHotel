package hotel.controlador;

import hotel.modelo.canal.Canal;
import hotel.excepcion.NegocioException;
import hotel.modelo.facturacion.MetodoPago;
import hotel.modelo.habitacion.HabitacionLibre;
import hotel.modelo.reserva.RegistroHistorial;
import hotel.modelo.reserva.Reserva;
import hotel.modelo.reserva.ReservaMemento;
import hotel.modelo.reserva.SolicitudReserva;
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

    void registrarAnticipo(Reserva reserva, double monto, MetodoPago metodo, String referencia,
            Integer idUsuario) throws NegocioException;

    List<Reserva> listar(String estado);

    List<RegistroHistorial> historial(int idReserva);

    void confirmar(Reserva reserva, Integer idUsuario) throws NegocioException;

    boolean cancelar(Reserva reserva, Integer idUsuario) throws NegocioException;

    void marcarNoShow(Reserva reserva, Integer idUsuario) throws NegocioException;

    void restaurarEstado(Reserva reserva, ReservaMemento memento, Integer idUsuario,
            String detalle) throws NegocioException;
}
