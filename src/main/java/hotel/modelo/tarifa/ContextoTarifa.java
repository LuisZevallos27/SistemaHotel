package hotel.modelo.tarifa;

import hotel.modelo.canal.Canal;
import hotel.modelo.habitacion.TipoHabitacion;
import java.util.List;

public record ContextoTarifa(TipoHabitacion tipo, int noches, Descuentos descuentos,
        Promocion promocion, List<Temporada> temporadas, Canal canal) {

}
