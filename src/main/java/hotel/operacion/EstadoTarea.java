package hotel.operacion;

public enum EstadoTarea {
    PENDIENTE, EN_PROCESO, TERMINADA;

    public boolean esFinal() {
        return this == TERMINADA;
    }

    public EstadoTarea siguiente() {
        if (this == PENDIENTE) {
            return EN_PROCESO;
        }
        if (this == EN_PROCESO) {
            return TERMINADA;
        }
        throw new IllegalStateException("La tarea ya está en su estado final.");
    }
}
