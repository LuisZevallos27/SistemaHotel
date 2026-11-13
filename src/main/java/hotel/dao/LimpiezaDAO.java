package hotel.dao;

import hotel.operacion.Empleado;
import hotel.operacion.EstadoTarea;
import hotel.operacion.TareaLimpieza;
import java.util.List;

public interface LimpiezaDAO {

    List<TareaLimpieza> listarTareas(String estado);

    List<Empleado> usuarios();

    void asignar(int idTarea, int idUsuario);

    void cambiarEstado(int idTarea, EstadoTarea estado);
}
