package hotel.reserva;

public class EstadoCheckIn extends EstadoReservaBase {

    @Override
    public String nombre() {
        return "CHECKIN";
    }

    @Override
    public EstadoReserva registrarCheckOut() {
        return EstadosReserva.CHECKOUT;
    }
}
