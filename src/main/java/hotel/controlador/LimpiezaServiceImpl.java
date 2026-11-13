package hotel.controlador;

import hotel.dao.LimpiezaDAO;
import hotel.excepcion.NegocioException;
import hotel.modelo.operacion.Empleado;
import hotel.modelo.operacion.EstadoTarea;
import hotel.modelo.operacion.TareaLimpieza;
import java.util.ArrayList;
import java.util.List;

public class LimpiezaServiceImpl implements LimpiezaService {

    private static final String ROL_LIMPIEZA = "Housekeeping";

    private final LimpiezaDAO dao;

    public LimpiezaServiceImpl(LimpiezaDAO dao) {
        this.dao = dao;
    }

    @Override
    public List<String> estados() {
        List<String> nombres = new ArrayList<>();
        for (EstadoTarea e : EstadoTarea.values()) {
            nombres.add(e.name());
        }
        return nombres;
    }

    @Override
    public List<TareaLimpieza> tareas(String estado) {
        boolean todas = estado == null || estado.trim().isEmpty() || "TODAS".equals(estado);
        return dao.listarTareas(todas ? null : estado);
    }

    @Override
    public List<Empleado> personal() {
        List<Empleado> personal = new ArrayList<>();
        for (Empleado e : dao.usuarios()) {
            if (e.activo() && ROL_LIMPIEZA.equalsIgnoreCase(e.rol())) {
                personal.add(e);
            }
        }
        return personal;
    }

    @Override
    public void asignar(TareaLimpieza tarea, Empleado empleado) throws NegocioException {
        if (tarea.estado().esFinal()) {
            throw new NegocioException("No se puede asignar una tarea terminada.");
        }
        dao.asignar(tarea.idTarea(), empleado.id());
    }

    @Override
    public EstadoTarea avanzar(TareaLimpieza tarea) throws NegocioException {
        if (tarea.estado().esFinal()) {
            throw new NegocioException("La tarea ya está terminada.");
        }
        if (tarea.estado() == EstadoTarea.PENDIENTE && tarea.asignado() == null) {
            throw new NegocioException("Asigne la tarea a una persona antes de iniciarla.");
        }
        EstadoTarea siguiente = tarea.estado().siguiente();
        dao.cambiarEstado(tarea.idTarea(), siguiente);
        return siguiente;
    }
}
