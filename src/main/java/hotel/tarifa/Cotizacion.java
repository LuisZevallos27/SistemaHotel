package hotel.tarifa;

import java.time.LocalDate;
import java.util.List;

public record Cotizacion(String tipo, LocalDate entrada, LocalDate salida, int noches,
        List<String> calculoPrimeraNoche, List<String> precioPorNoche, double total) {

}
