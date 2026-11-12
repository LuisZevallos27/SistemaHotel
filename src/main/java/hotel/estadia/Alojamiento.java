package hotel.estadia;

import java.util.List;

public record Alojamiento(String descripcion, double total) implements ElementoCuenta {

    @Override
    public List<ElementoCuenta> hijos() {
        return List.of();
    }
}
