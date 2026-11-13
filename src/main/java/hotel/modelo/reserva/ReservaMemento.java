package hotel.modelo.reserva;

public final class ReservaMemento {

    private final EstadoReserva estado;

    ReservaMemento(EstadoReserva estado) {
        this.estado = estado;
    }

    EstadoReserva estado() {
        return estado;
    }
}
