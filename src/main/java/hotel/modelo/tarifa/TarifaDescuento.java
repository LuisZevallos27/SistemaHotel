package hotel.modelo.tarifa;

import java.util.Locale;

public class TarifaDescuento extends TarifaDecorator {

    private final String etiqueta;
    private final double porcentaje;

    public TarifaDescuento(Tarifa interna, String etiqueta, double porcentaje) {
        super(interna);
        this.etiqueta = etiqueta;
        this.porcentaje = porcentaje;
    }

    @Override
    public double precioNoche() {
        return redondear(interna.precioNoche() * (1 - porcentaje / 100.0));
    }

    @Override
    protected String descripcion() {
        return String.format(Locale.US, "%s (-%.0f%%)", etiqueta, porcentaje);
    }
}
