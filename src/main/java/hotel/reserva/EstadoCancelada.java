package hotel.reserva;

public class EstadoCancelada extends EstadoReservaBase {

    @Override
    public String nombre() {
        return "CANCELADA";
    }
}
