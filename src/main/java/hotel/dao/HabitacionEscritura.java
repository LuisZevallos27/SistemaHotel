package hotel.dao;

import hotel.modelo.habitacion.EstadoHabitacion;

public interface HabitacionEscritura {

    void actualizarEstado(int idHabitacion, EstadoHabitacion estado);
}
