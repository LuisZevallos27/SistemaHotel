package hotel.modelo.habitacion;

public class HabitacionSuite extends Habitacion {

    public HabitacionSuite(DatosHabitacion datos) {
        super(datos);
    }

    @Override
    public String getTipo() {
        return "Suite";
    }

    @Override
    public int getHoraCheckOut() {
        return 14;
    }

    @Override
    public boolean incluyeDesayuno() {
        return true;
    }
}
