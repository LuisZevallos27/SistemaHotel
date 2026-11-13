package hotel.modelo.operacion;

import java.time.LocalDateTime;

public record Mantenimiento(int idMantenimiento, int idHabitacion, String habitacion,
        String descripcion, Prioridad prioridad, String estado,
        LocalDateTime fechaReporte, LocalDateTime fechaCierre, String reporta) {

    public boolean estaCerrado() {
        return "CERRADO".equals(estado);
    }
}
