package hotel.modelo.reserva;

public class EstadoConfirmada extends EstadoReservaBase {

    @Override
    public String nombre() {
        return "CONFIRMADA";
    }

    @Override
    public EstadoReserva cancelar() {
        return EstadosReserva.CANCELADA;
    }

    @Override
    public EstadoReserva marcarNoShow() {
        return EstadosReserva.NO_SHOW;
    }

    @Override
    public EstadoReserva registrarCheckIn() {
        return EstadosReserva.CHECKIN;
    }

    @Override
    public boolean admiteModificacion() {
        return true;
    }

    @Override
    public boolean ocupaHabitacion() {
        return true;
    }
}
