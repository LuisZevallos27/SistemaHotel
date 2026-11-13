package hotel.dao;

import hotel.modelo.habitacion.HabitacionLibre;
import java.time.LocalDate;
import java.util.List;

public interface DisponibilidadConsulta {

    List<HabitacionLibre> buscar(int idTipo, LocalDate entrada, LocalDate salida);
}
