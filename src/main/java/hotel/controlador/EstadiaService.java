package hotel.controlador;

import hotel.modelo.estadia.Cuenta;
import hotel.modelo.estadia.Estadia;
import hotel.modelo.estadia.Servicio;
import hotel.excepcion.NegocioException;
import hotel.modelo.habitacion.HabitacionLibre;
import hotel.modelo.reserva.Reserva;
import java.util.List;

public interface EstadiaService {

    List<Reserva> llegadasDeHoy();

    List<HabitacionLibre> habitacionesParaCheckIn(Reserva reserva);

    int registrarCheckIn(Reserva reserva, int idHabitacion, Integer idUsuario) throws NegocioException;

    List<Estadia> estadiasEnCurso();

    List<Servicio> servicios();

    void registrarConsumo(Estadia estadia, Servicio servicio, int cantidad, Integer idUsuario)
            throws NegocioException;

    Cuenta cuentaDe(Estadia estadia);
}
