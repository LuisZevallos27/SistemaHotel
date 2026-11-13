package hotel.modelo.habitacion;

public enum EstadoHabitacion {
    DISPONIBLE, RESERVADA, OCUPADA, LIMPIEZA, MANTENIMIENTO;

    public static EstadoHabitacion desde(String texto) {
        return valueOf(texto.trim().toUpperCase());
    }

    public boolean esManual() {
        return this == DISPONIBLE || this == LIMPIEZA || this == MANTENIMIENTO;
    }
}
