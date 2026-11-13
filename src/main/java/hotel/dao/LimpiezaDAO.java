package hotel.dao;

import hotel.modelo.operacion.Empleado;
import hotel.modelo.operacion.EstadoTarea;
import hotel.modelo.operacion.TareaLimpieza;
import java.util.List;

public interface LimpiezaDAO {

    List<TareaLimpieza> listarTareas(String estado);

    List<Empleado> usuarios();

    void asignar(int idTarea, int idUsuario);

    void cambiarEstado(int idTarea, EstadoTarea estado);
}
