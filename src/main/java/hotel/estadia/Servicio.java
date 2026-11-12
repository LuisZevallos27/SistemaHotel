package hotel.estadia;

import java.util.Locale;

public record Servicio(int idServicio, String nombre, String area, double precio) {

    @Override
    public String toString() {
        return String.format(Locale.US, "%s (%s) - S/ %.2f", nombre, area, precio);
    }
}
