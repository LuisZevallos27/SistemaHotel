package hotel.habitacion;

public class FabricaHabitacionFamiliar implements FabricaHabitacion {

    @Override
    public String tipo() {
        return "Familiar";
    }

    @Override
    public Habitacion crear(DatosHabitacion datos) {
        return new HabitacionFamiliar(datos);
    }
}
