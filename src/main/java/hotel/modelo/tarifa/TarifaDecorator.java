package hotel.modelo.tarifa;

import java.util.List;
import java.util.Locale;

public abstract class TarifaDecorator implements Tarifa {

    protected final Tarifa interna;

    protected TarifaDecorator(Tarifa interna) {
        this.interna = interna;
    }

    protected abstract String descripcion();

    protected static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    @Override
    public List<String> detalle() {
        List<String> lineas = interna.detalle();
        lineas.add(String.format(Locale.US, "%s: S/ %.2f", descripcion(), precioNoche()));
        return lineas;
    }
}
