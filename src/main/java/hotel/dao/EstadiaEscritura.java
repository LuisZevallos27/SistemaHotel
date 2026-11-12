package hotel.dao;

public interface EstadiaEscritura {

    int registrarCheckIn(int idReserva, int idHabitacion, Integer idUsuario);

    void registrarConsumo(int idEstadia, int idServicio, int cantidad, Integer idUsuario);
}
