package hotel.servicio;

import hotel.excepcion.NegocioException;
import hotel.habitacion.Habitacion;
import hotel.operacion.Mantenimiento;
import hotel.operacion.Prioridad;
import java.util.List;

public interface MantenimientoService {

    List<String> estados();

    List<Mantenimiento> listar(String estado);

    void reportar(Habitacion habitacion, String descripcion, Prioridad prioridad, Integer idUsuario)
            throws NegocioException;

    void cerrar(Mantenimiento mantenimiento) throws NegocioException;
}
