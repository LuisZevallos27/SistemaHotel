package hotel.reserva;

public class EstadoPendiente extends EstadoReservaBase {

    @Override
    public String nombre() {
        return "PENDIENTE";
    }

    @Override
    public EstadoReserva confirmar() {
        return EstadosReserva.CONFIRMADA;
    }

    @Override
    public EstadoReserva cancelar() {
        return EstadosReserva.CANCELADA;
    }

    @Override
    public boolean admiteModificacion() {
        return true;
    }
}
