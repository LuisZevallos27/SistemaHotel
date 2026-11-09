package hotel.habitacion;

public interface FabricaHabitacion {

    String tipo();

    Habitacion crear(DatosHabitacion datos);
}
