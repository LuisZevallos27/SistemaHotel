package hotel.modelo.habitacion;

public class HabitacionFamiliar extends Habitacion {

    public HabitacionFamiliar(DatosHabitacion datos) {
        super(datos);
    }

    @Override
    public String getTipo() {
        return "Familiar";
    }

    @Override
    public int getHoraCheckOut() {
        return 13;
    }
}
