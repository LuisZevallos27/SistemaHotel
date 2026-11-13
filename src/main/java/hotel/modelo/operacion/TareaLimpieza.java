package hotel.modelo.operacion;

import java.time.LocalDateTime;

public record TareaLimpieza(int idTarea, int idHabitacion, String habitacion, String tipo,
        EstadoTarea estado, String asignado, LocalDateTime fechaCreacion) {

}
