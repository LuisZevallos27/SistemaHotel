package hotel.servicio;

import hotel.excepcion.NegocioException;
import hotel.operacion.Empleado;
import hotel.operacion.EstadoTarea;
import hotel.operacion.TareaLimpieza;
import java.util.List;

public interface LimpiezaService {

    List<String> estados();

    List<TareaLimpieza> tareas(String estado);

    List<Empleado> personal();

    void asignar(TareaLimpieza tarea, Empleado empleado) throws NegocioException;

    EstadoTarea avanzar(TareaLimpieza tarea) throws NegocioException;
}
