package hotel.tarifa;

import java.time.LocalDate;

public class FabricaTarifaPromocion implements FabricaTarifa {

    @Override
    public Tarifa decorar(Tarifa tarifa, ContextoTarifa contexto, LocalDate fecha) {
        if (contexto.promocion() != null) {
            return new TarifaPromocion(tarifa, contexto.promocion());
        }
        return tarifa;
    }
}
