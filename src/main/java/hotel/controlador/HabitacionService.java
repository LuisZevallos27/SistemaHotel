package hotel.controlador;

import hotel.excepcion.NegocioException;
import hotel.modelo.habitacion.EstadoHabitacion;
import hotel.modelo.habitacion.Habitacion;
import java.util.List;

public interface HabitacionService {

    List<Habitacion> listar();

    List<EstadoHabitacion> estadosManuales();

    void cambiarEstado(Habitacion habitacion, EstadoHabitacion nuevo) throws NegocioException;
}
