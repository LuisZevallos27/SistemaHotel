package hotel.tarifa;

import hotel.canal.Canal;
import java.time.LocalDate;

public class FabricaTarifaCanal implements FabricaTarifa {

    @Override
    public Tarifa decorar(Tarifa tarifa, ContextoTarifa contexto, LocalDate fecha) {
        Canal canal = contexto.canal();
        if (canal != null && canal.descuentoPorcentaje() > 0) {
            return new TarifaDescuento(tarifa, "Canal " + canal.nombre(), canal.descuentoPorcentaje());
        }
        return tarifa;
    }
}
