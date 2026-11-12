package hotel.dao;

public interface EstadiaEscritura {

    int registrarCheckIn(int idReserva, int idHabitacion, Integer idUsuario);

    void registrarConsumo(int idEstadia, int idServicio, int cantidad, Integer idUsuario);

    void registrarCheckOut(int idEstadia, Integer idUsuario);

    void registrarEncuesta(int idEstadia, int puntaje, String comentario);
}
