package hotel.modelo.habitacion;

import java.util.List;

public abstract class Habitacion {

    private final DatosHabitacion datos;

    protected Habitacion(DatosHabitacion datos) {
        this.datos = datos;
    }

    public abstract String getTipo();

    public int getHoraCheckOut() {
        return 12;
    }

    public boolean incluyeDesayuno() {
        return false;
    }

    public int getId() {
        return datos.id();
    }

    public String getNumero() {
        return datos.numero();
    }

    public int getPiso() {
        return datos.piso();
    }

    public int getCapacidad() {
        return datos.capacidad();
    }

    public double getTarifaBase() {
        return datos.tarifaBase();
    }

    public EstadoHabitacion getEstado() {
        return datos.estado();
    }

    public List<String> getAmenidades() {
        return datos.amenidades();
    }
}
