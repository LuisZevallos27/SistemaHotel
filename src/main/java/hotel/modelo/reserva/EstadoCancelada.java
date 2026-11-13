package hotel.modelo.reserva;

public class EstadoCancelada extends EstadoReservaBase {

    @Override
    public String nombre() {
        return "CANCELADA";
    }
}
