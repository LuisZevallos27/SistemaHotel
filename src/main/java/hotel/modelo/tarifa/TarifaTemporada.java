package hotel.modelo.tarifa;

import java.util.Locale;

public class TarifaTemporada extends TarifaDecorator {

    private final String nombre;
    private final double factor;

    public TarifaTemporada(Tarifa interna, String nombre, double factor) {
        super(interna);
        this.nombre = nombre;
        this.factor = factor;
    }

    @Override
    public double precioNoche() {
        return redondear(interna.precioNoche() * factor);
    }

    @Override
    protected String descripcion() {
        return String.format(Locale.US, "%s (x%.2f)", nombre, factor);
    }
}
