package hotel.modelo.tarifa;

import java.time.LocalDate;

public class FabricaTarifaTemporada implements FabricaTarifa {

    @Override
    public Tarifa decorar(Tarifa tarifa, ContextoTarifa contexto, LocalDate fecha) {
        for (Temporada t : contexto.temporadas()) {
            if (t.cubre(fecha)) {
                return new TarifaTemporada(tarifa, t.nombre(), t.factor());
            }
        }
        return tarifa;
    }
}
