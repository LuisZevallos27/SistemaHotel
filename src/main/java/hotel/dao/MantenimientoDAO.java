package hotel.dao;

import hotel.operacion.Mantenimiento;
import hotel.operacion.Prioridad;
import java.util.List;

public interface MantenimientoDAO {

    List<Mantenimiento> listarMantenimientos(String estado);

    void reportar(int idHabitacion, String descripcion, Prioridad prioridad, Integer idUsuario);

    void cerrar(int idMantenimiento);
}
