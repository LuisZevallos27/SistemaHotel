package hotel.habitacion;

public class HabitacionMatrimonial extends Habitacion {

    public HabitacionMatrimonial(DatosHabitacion datos) {
        super(datos);
    }

    @Override
    public String getTipo() {
        return "Matrimonial";
    }
}
