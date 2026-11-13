package hotel.modelo.habitacion;

public record HabitacionLibre(int id, String numero, int piso) {

    @Override
    public String toString() {
        return "Hab. " + numero + " (piso " + piso + ")";
    }
}
