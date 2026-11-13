package hotel.modelo.reserva;

public class EstadoCheckOut extends EstadoReservaBase {

    @Override
    public String nombre() {
        return "CHECKOUT";
    }
}
