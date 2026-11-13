package hotel.modelo.habitacion;

import java.util.List;

public record DatosHabitacion(int id, String numero, int piso, EstadoHabitacion estado,
        int capacidad, double tarifaBase, List<String> amenidades) {

}
