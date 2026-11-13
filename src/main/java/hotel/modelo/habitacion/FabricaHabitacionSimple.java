package hotel.modelo.habitacion;

public class FabricaHabitacionSimple implements FabricaHabitacion {

    @Override
    public String tipo() {
        return "Simple";
    }

    @Override
    public Habitacion crear(DatosHabitacion datos) {
        return new HabitacionSimple(datos);
    }
}
