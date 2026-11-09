package hotel.habitacion;

public class FabricaHabitacionDoble implements FabricaHabitacion {

    @Override
    public String tipo() {
        return "Doble";
    }

    @Override
    public Habitacion crear(DatosHabitacion datos) {
        return new HabitacionDoble(datos);
    }
}
