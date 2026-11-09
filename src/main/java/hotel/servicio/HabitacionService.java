package hotel.servicio;

import hotel.excepcion.NegocioException;
import hotel.habitacion.EstadoHabitacion;
import hotel.habitacion.Habitacion;
import java.util.List;

public interface HabitacionService {

    List<Habitacion> listar();

    List<EstadoHabitacion> estadosManuales();

    void cambiarEstado(Habitacion habitacion, EstadoHabitacion nuevo) throws NegocioException;
}
