package hotel.modelo.tarifa;

import java.time.LocalDate;

public record Promocion(int id, String codigo, String nombre, String tipoDescuento, double valor,
        LocalDate inicio, LocalDate fin, int minNoches, Integer idTipo) {

    public boolean esPorcentaje() {
        return "PORCENTAJE".equals(tipoDescuento);
    }
}
