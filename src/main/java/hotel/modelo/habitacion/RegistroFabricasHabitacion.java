package hotel.modelo.habitacion;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RegistroFabricasHabitacion implements CreadorHabitaciones {

    private final Map<String, FabricaHabitacion> fabricas = new HashMap<>();

    public RegistroFabricasHabitacion() {
        registrar(new FabricaHabitacionSimple());
        registrar(new FabricaHabitacionDoble());
        registrar(new FabricaHabitacionMatrimonial());
        registrar(new FabricaHabitacionSuite());
        registrar(new FabricaHabitacionFamiliar());
    }

    public final void registrar(FabricaHabitacion fabrica) {
        fabricas.put(fabrica.tipo().toLowerCase(), fabrica);
    }

    @Override
    public Optional<Habitacion> crear(String tipo, DatosHabitacion datos) {
        FabricaHabitacion fabrica = fabricas.get(tipo.trim().toLowerCase());
        if (fabrica == null) {
            return Optional.empty();
        }
        return Optional.of(fabrica.crear(datos));
    }
}
