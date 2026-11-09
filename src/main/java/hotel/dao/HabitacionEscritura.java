package hotel.dao;

import hotel.habitacion.EstadoHabitacion;

public interface HabitacionEscritura {

    void actualizarEstado(int idHabitacion, EstadoHabitacion estado);
}
