package hotel.tarifa;

import hotel.canal.Canal;
import hotel.habitacion.TipoHabitacion;
import java.util.List;

public record ContextoTarifa(TipoHabitacion tipo, int noches, Descuentos descuentos,
        Promocion promocion, List<Temporada> temporadas, Canal canal) {

}
