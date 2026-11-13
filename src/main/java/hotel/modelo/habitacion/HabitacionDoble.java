package hotel.modelo.habitacion;

public class HabitacionDoble extends Habitacion {

    public HabitacionDoble(DatosHabitacion datos) {
        super(datos);
    }

    @Override
    public String getTipo() {
        return "Doble";
    }
}
