package hotel.habitacion;

public class FabricaHabitacionMatrimonial implements FabricaHabitacion {

    @Override
    public String tipo() {
        return "Matrimonial";
    }

    @Override
    public Habitacion crear(DatosHabitacion datos) {
        return new HabitacionMatrimonial(datos);
    }
}
