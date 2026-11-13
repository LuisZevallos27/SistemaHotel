package hotel.controlador;

import hotel.excepcion.NegocioException;
import hotel.modelo.operacion.Empleado;
import hotel.modelo.operacion.EstadoTarea;
import hotel.modelo.operacion.TareaLimpieza;
import java.util.List;

public interface LimpiezaService {

    List<String> estados();

    List<TareaLimpieza> tareas(String estado);

    List<Empleado> personal();

    void asignar(TareaLimpieza tarea, Empleado empleado) throws NegocioException;

    EstadoTarea avanzar(TareaLimpieza tarea) throws NegocioException;
}
