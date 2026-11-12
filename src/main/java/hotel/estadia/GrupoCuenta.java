package hotel.estadia;

import java.util.ArrayList;
import java.util.List;

public class GrupoCuenta implements ElementoCuenta {

    private final String descripcion;
    private final List<ElementoCuenta> hijos = new ArrayList<>();

    public GrupoCuenta(String descripcion) {
        this.descripcion = descripcion;
    }

    public void agregar(ElementoCuenta elemento) {
        hijos.add(elemento);
    }

    @Override
    public String descripcion() {
        return descripcion;
    }

    @Override
    public double total() {
        double suma = 0;
        for (ElementoCuenta hijo : hijos) {
            suma += hijo.total();
        }
        return Math.round(suma * 100.0) / 100.0;
    }

    @Override
    public List<ElementoCuenta> hijos() {
        return List.copyOf(hijos);
    }
}
