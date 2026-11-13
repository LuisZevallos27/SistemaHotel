package hotel.controlador;

import hotel.excepcion.NegocioException;
import hotel.modelo.habitacion.Habitacion;
import hotel.modelo.operacion.Mantenimiento;
import hotel.modelo.operacion.Prioridad;
import java.util.List;

public interface MantenimientoService {

    List<String> estados();

    List<Mantenimiento> listar(String estado);

    void reportar(Habitacion habitacion, String descripcion, Prioridad prioridad, Integer idUsuario)
            throws NegocioException;

    void cerrar(Mantenimiento mantenimiento) throws NegocioException;
}
