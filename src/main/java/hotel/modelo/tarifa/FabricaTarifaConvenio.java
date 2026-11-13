package hotel.modelo.tarifa;

import java.time.LocalDate;

public class FabricaTarifaConvenio implements FabricaTarifa {

    @Override
    public Tarifa decorar(Tarifa tarifa, ContextoTarifa contexto, LocalDate fecha) {
        Descuentos d = contexto.descuentos();
        if (d.descuentoEmpresa() > 0) {
            return new TarifaDescuento(tarifa, "Convenio " + d.empresa(), d.descuentoEmpresa());
        }
        return tarifa;
    }
}
