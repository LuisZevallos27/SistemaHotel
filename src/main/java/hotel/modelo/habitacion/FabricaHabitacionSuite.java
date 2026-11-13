package hotel.modelo.habitacion;

public class FabricaHabitacionSuite implements FabricaHabitacion {

    @Override
    public String tipo() {
        return "Suite";
    }

    @Override
    public Habitacion crear(DatosHabitacion datos) {
        return new HabitacionSuite(datos);
    }
}
