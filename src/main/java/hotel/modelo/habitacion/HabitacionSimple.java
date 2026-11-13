package hotel.modelo.habitacion;

public class HabitacionSimple extends Habitacion {

    public HabitacionSimple(DatosHabitacion datos) {
        super(datos);
    }

    @Override
    public String getTipo() {
        return "Simple";
    }
}
