package hotel.tarifa;

import java.time.LocalDate;

public record Temporada(int id, String nombre, LocalDate inicio, LocalDate fin, double factor) {

    public boolean cubre(LocalDate fecha) {
        return !fecha.isBefore(inicio) && !fecha.isAfter(fin);
    }
}
