package hotel.modelo.habitacion;

import java.util.Optional;

public interface CreadorHabitaciones {

    Optional<Habitacion> crear(String tipo, DatosHabitacion datos);
}
