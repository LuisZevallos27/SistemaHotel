package hotel.controlador;

import hotel.dao.HabitacionConsulta;
import hotel.dao.HabitacionEscritura;
import hotel.excepcion.NegocioException;
import hotel.modelo.habitacion.EstadoHabitacion;
import hotel.modelo.habitacion.Habitacion;
import java.util.ArrayList;
import java.util.List;

public class HabitacionServiceImpl implements HabitacionService {

    private final HabitacionConsulta consulta;
    private final HabitacionEscritura escritura;

    public HabitacionServiceImpl(HabitacionConsulta consulta, HabitacionEscritura escritura) {
        this.consulta = consulta;
        this.escritura = escritura;
    }

    @Override
    public List<Habitacion> listar() {
        return consulta.listar();
    }

    @Override
    public List<EstadoHabitacion> estadosManuales() {
        List<EstadoHabitacion> manuales = new ArrayList<>();
        for (EstadoHabitacion e : EstadoHabitacion.values()) {
            if (e.esManual()) {
                manuales.add(e);
            }
        }
        return manuales;
    }

    @Override
    public void cambiarEstado(Habitacion habitacion, EstadoHabitacion nuevo) throws NegocioException {
        if (!nuevo.esManual()) {
            throw new NegocioException(
                    "Los estados RESERVADA y OCUPADA solo cambian desde reservas y estadías.");
        }
        if (!habitacion.getEstado().esManual()) {
            throw new NegocioException("La habitación " + habitacion.getNumero() + " está "
                    + habitacion.getEstado() + " y no se puede cambiar manualmente.");
        }
        if (habitacion.getEstado() == nuevo) {
            throw new NegocioException("La habitación ya está en estado " + nuevo + ".");
        }
        escritura.actualizarEstado(habitacion.getId(), nuevo);
    }
}
