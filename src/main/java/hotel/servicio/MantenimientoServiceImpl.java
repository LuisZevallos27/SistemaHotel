package hotel.servicio;

import hotel.dao.MantenimientoDAO;
import hotel.excepcion.NegocioException;
import hotel.habitacion.Habitacion;
import hotel.operacion.Mantenimiento;
import hotel.operacion.Prioridad;
import java.util.List;

public class MantenimientoServiceImpl implements MantenimientoService {

    private static final int MINIMO_DESCRIPCION = 5;

    private final MantenimientoDAO dao;

    public MantenimientoServiceImpl(MantenimientoDAO dao) {
        this.dao = dao;
    }

    @Override
    public List<String> estados() {
        return List.of("ABIERTO", "EN_PROCESO", "CERRADO");
    }

    @Override
    public List<Mantenimiento> listar(String estado) {
        boolean todos = estado == null || estado.trim().isEmpty() || "TODOS".equals(estado);
        return dao.listarMantenimientos(todos ? null : estado);
    }

    @Override
    public void reportar(Habitacion habitacion, String descripcion, Prioridad prioridad,
            Integer idUsuario) throws NegocioException {
        if (descripcion == null || descripcion.trim().length() < MINIMO_DESCRIPCION) {
            throw new NegocioException("Describa el problema (mínimo " + MINIMO_DESCRIPCION + " caracteres).");
        }
        dao.reportar(habitacion.getId(), descripcion.trim(), prioridad, idUsuario);
    }

    @Override
    public void cerrar(Mantenimiento mantenimiento) throws NegocioException {
        if (mantenimiento.estaCerrado()) {
            throw new NegocioException("El reporte ya está cerrado.");
        }
        dao.cerrar(mantenimiento.idMantenimiento());
    }
}
