package hotel.servicio;

import hotel.estadia.Cuenta;
import hotel.estadia.Estadia;
import hotel.estadia.Servicio;
import hotel.excepcion.NegocioException;
import hotel.habitacion.HabitacionLibre;
import hotel.reserva.Reserva;
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
