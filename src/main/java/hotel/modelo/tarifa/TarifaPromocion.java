package hotel.modelo.tarifa;

import java.util.Locale;

public class TarifaPromocion extends TarifaDecorator {

    private final Promocion promocion;

    public TarifaPromocion(Tarifa interna, Promocion promocion) {
        super(interna);
        this.promocion = promocion;
    }

    @Override
    public double precioNoche() {
        double base = interna.precioNoche();
        if (promocion.esPorcentaje()) {
            return redondear(base * (1 - promocion.valor() / 100.0));
        }
        return redondear(Math.max(0, base - promocion.valor()));
    }

    @Override
    protected String descripcion() {
        String valor = promocion.esPorcentaje()
                ? String.format(Locale.US, "-%.0f%%", promocion.valor())
                : String.format(Locale.US, "-S/ %.2f por noche", promocion.valor());
        return "Promoción " + promocion.codigo() + " (" + valor + ")";
    }
}
