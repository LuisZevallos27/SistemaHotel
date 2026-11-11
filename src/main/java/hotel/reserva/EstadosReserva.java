package hotel.reserva;

import java.util.ArrayList;
import java.util.List;

public final class EstadosReserva {

    public static final EstadoReserva PENDIENTE = new EstadoPendiente();
    public static final EstadoReserva CONFIRMADA = new EstadoConfirmada();
    public static final EstadoReserva CHECKIN = new EstadoCheckIn();
    public static final EstadoReserva CHECKOUT = new EstadoCheckOut();
    public static final EstadoReserva CANCELADA = new EstadoCancelada();
    public static final EstadoReserva NO_SHOW = new EstadoNoShow();

    private static final List<EstadoReserva> TODOS
            = List.of(PENDIENTE, CONFIRMADA, CHECKIN, CHECKOUT, CANCELADA, NO_SHOW);

    private EstadosReserva() {
    }

    public static EstadoReserva desde(String nombre) {
        for (EstadoReserva e : TODOS) {
            if (e.nombre().equals(nombre)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Estado de reserva desconocido: " + nombre);
    }

    public static List<String> nombres() {
        List<String> nombres = new ArrayList<>();
        for (EstadoReserva e : TODOS) {
            nombres.add(e.nombre());
        }
        return nombres;
    }
}
