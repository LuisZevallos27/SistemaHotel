package hotel.modelo.tarifa;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TarifaBase implements Tarifa {

    private final double precio;

    public TarifaBase(double precio) {
        this.precio = precio;
    }

    @Override
    public double precioNoche() {
        return precio;
    }

    @Override
    public List<String> detalle() {
        List<String> lineas = new ArrayList<>();
        lineas.add(String.format(Locale.US, "Tarifa base: S/ %.2f", precio));
        return lineas;
    }
}
