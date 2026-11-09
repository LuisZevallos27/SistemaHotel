package hotel.tarifa;

import java.time.LocalDate;

public interface FabricaTarifa {

    Tarifa decorar(Tarifa tarifa, ContextoTarifa contexto, LocalDate fecha);
}
