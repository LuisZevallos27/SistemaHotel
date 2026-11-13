package hotel.modelo.estadia;

import java.time.LocalDateTime;
import java.util.List;

public record Consumo(int id, String servicio, String area, int cantidad,
        double precioUnitario, LocalDateTime fecha) implements ElementoCuenta {

    @Override
    public String descripcion() {
        return cantidad + " x " + servicio;
    }

    @Override
    public double total() {
        return Math.round(cantidad * precioUnitario * 100.0) / 100.0;
    }

    @Override
    public List<ElementoCuenta> hijos() {
        return List.of();
    }
}
